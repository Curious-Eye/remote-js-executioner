package pragmasoft.andriilupynos.js_executioner.service;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.graalvm.polyglot.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pragmasoft.andriilupynos.js_executioner.data.TaskStore;
import pragmasoft.andriilupynos.js_executioner.data.domain.Task;
import pragmasoft.andriilupynos.js_executioner.data.domain.TaskStatus;
import pragmasoft.andriilupynos.js_executioner.exception.IllegalArgumentException;
import pragmasoft.andriilupynos.js_executioner.exception.TaskNotFoundException;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.StringWriter;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

@Service
public class TaskExecuteService {

    private final Logger log = LoggerFactory.getLogger(TaskExecuteService.class);

    @Autowired private TaskStore taskStore;

    private final Scheduler scheduler =
            Schedulers.newParallel(TaskExecuteService.class.getName());
    private final ConcurrentHashMap<String, ExecutionContext> taskIdAndExecution = new ConcurrentHashMap<>();

    /**
     * Periodically schedules tasks for execution
     */
    @Scheduled(fixedRate = 2000)
    public void checkTasksAndExecute() {
        taskStore.findAllByStatusIn(List.of(TaskStatus.EXECUTING, TaskStatus.SCHEDULED, TaskStatus.NEW))
                .filter(task -> !taskIdAndExecution.containsKey(task.getId()))
                .filter(task -> task.getScheduledAt() == null || !task.getScheduledAt().after(new Date()))
                .flatMap(task -> {
                    task.setStatus(TaskStatus.SCHEDULED);
                    return taskStore.save(task);
                })
                .doOnNext(task -> {
                    var taskOutputWriter = new StringWriter();
                    taskIdAndExecution.put(
                            task.getId(),
                            ExecutionContext.builder()
                                    .execution(scheduler.schedule(() -> this.execute(task, taskOutputWriter).subscribe()))
                                    .currentOutputWriter(taskOutputWriter)
                                    .build()
                    );
                    log.debug("Scheduled task {} for execution", task.getId());
                })
                .then()
                .block();
    }

    /**
     * Execute given task and write its output to outputWriter
     *
     * @param task task to execute
     * @param outputWriter writer to write task's output to
     * @return Mono indicating when task is executed
     */
    public Mono<Void> execute(Task task, StringWriter outputWriter) {
        log.debug("Executing task {}", task.getId());
        task.setStatus(TaskStatus.EXECUTING);
        task.setBeginExecDate(new Date());
        return taskStore.save(task)
                .then(executeCode(task, outputWriter))
                .flatMap(executeRes -> {
                    task.setStatus(
                            executeRes.errored ? TaskStatus.ERRORED :
                                    executeRes.stopped ? TaskStatus.STOPPED : TaskStatus.COMPLETED
                    );
                    task.setOutput(executeRes.getOutput());
                    task.setError(executeRes.getError());
                    task.setEndExecDate(new Date());
                    return taskStore.save(task);
                })
                .doOnNext(it -> {
                    taskIdAndExecution.remove(it.getId());
                    log.debug("Done executing task {}", it.getId());
                })
                .then();
    }

    private Mono<ExecuteCodeResult> executeCode(Task task, StringWriter outputWriter) {
        var engine = GraalJSScriptEngine.create(null, Context.newBuilder("js"));
        engine.getContext().setWriter(outputWriter);
        return Mono.fromCallable(() -> engine.eval(task.getCode()))
                .then(Mono.fromCallable(() ->
                        ExecuteCodeResult.builder().output(outputWriter.toString()).build()))
                .timeout(Duration.ofSeconds(30))
                .doOnError(err -> log.error("Error evaluating script {}: {}", task.getId(), err.getMessage()))
                .onErrorResume(err -> Mono.just(buildExecResultOnError(outputWriter, err)));
    }

    private ExecuteCodeResult buildExecResultOnError(StringWriter scriptOutput, Throwable err) {
        var resBuilder =
                ExecuteCodeResult.builder().output(scriptOutput.toString());

        if (err.getClass().isAssignableFrom(TimeoutException.class)) {
            return resBuilder.error("Script timed-out. Maximum execution duration is 30 seconds")
                    .errored(true)
                    .build();
        }
        if (err.getCause().getMessage().equals("Thread was interrupted.")) {
            return resBuilder.stopped(true).build();
        }

        return resBuilder.error(err.getCause().getMessage())
                .errored(true)
                .build();
    }

    /**
     * Returns current execution output for task with requested id if
     * it is currently being executed or null
     *
     * @param taskId id of the task to return
     * @return Execution output or null in case task is not being executed
     */
    public String getCurrentExecutionOutput(String taskId) {
        var exec = taskIdAndExecution.get(taskId);
        if (exec == null)
            return null;
        return exec.currentOutputWriter.toString();
    }

    /**
     * Stop task by id. This method does nothing if task with such id is not being executed.
     * Returns Mono.error(TaskNotFoundException) if task with such id does not exist
     *
     * @param taskId Id of the task
     * @return Mono signaling when operation completes
     */
    public Mono<Void> changeExecution(String taskId, ChangeExecutionModel model) {
        log.debug("Stopping task {}", taskId);
        if (model.getAction() != ChangeExecutionAction.STOP) {
            return Mono.error(
                    new IllegalArgumentException(
                            "Cannot change execution: Unexpected action passed - " + model.getAction()
                    )
            );
        }
        return taskStore.findById(taskId)
                .switchIfEmpty(Mono.error(new TaskNotFoundException("Task with such id does not exist")))
                .mapNotNull(unused -> {
                    var taskExecutionState = taskIdAndExecution.get(taskId);
                    if (taskExecutionState != null) {
                        taskExecutionState.getExecution().dispose();
                    }
                    return null;
                });
    }

    /**
     * Stop all current tasks.
     *
     * @return Mono signaling when operation completes
     */
    public Mono<Void> stopAll() {
        log.debug("Stopping all current tasks");
        return Mono.fromCallable(() -> {
            taskIdAndExecution.values().forEach(exec -> exec.getExecution().dispose());
            return null;
        });
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ExecuteCodeResult {
        private String output;
        private String error;
        private boolean errored;
        private boolean stopped;
    }

    /**
     * This class is needed to allow access to task's output during its execution
     * and to be able to stop its execution if needed.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ExecutionContext {
        private StringWriter currentOutputWriter;
        private Disposable execution;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeExecutionModel {
        private ChangeExecutionAction action;
    }

    public enum ChangeExecutionAction {
        STOP
    }

}
