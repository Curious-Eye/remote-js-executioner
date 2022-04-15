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
            Schedulers.newBoundedElastic(4, 500, TaskExecuteService.class.getName());
    private final ConcurrentHashMap<String, ExecutionState> taskIdAndExecutionState = new ConcurrentHashMap<>();

    @Scheduled(initialDelay = 5000, fixedRate = 2000)
    public void checkTasksAndExecute() {
        taskStore.findAllByStatusIn(List.of(TaskStatus.EXECUTING, TaskStatus.NEW))
                .filter(task -> !taskIdAndExecutionState.containsKey(task.getId()))
                .filter(task -> task.getScheduledAt() == null || !task.getScheduledAt().after(new Date()))
                .doOnNext(task ->
                        taskIdAndExecutionState.put(
                                task.getId(),
                                ExecutionState.builder()
                                        .taskExecution(scheduler.schedule(() -> this.execute(task).block()))
                                        .output(new StringWriter())
                                        .build()
                        )
                )
                .then()
                .block();
    }

    /**
     * Execute given task
     * @param task - task to execute
     * @return - Mono, indicating when task is executed
     */
    public Mono<Void> execute(Task task) {
        log.debug("Executing task {}", task.getId());
        task.setStatus(TaskStatus.EXECUTING);
        task.setBeginExecDate(new Date());
        return taskStore.save(task)
                .flatMap(this::executeCode)
                .flatMap(executeRes -> {
                    task.setStatus(executeRes.errored ? TaskStatus.ERRORED : TaskStatus.COMPLETED);
                    task.setOutput(executeRes.getOutput());
                    task.setError(executeRes.getError());
                    task.setEndExecDate(new Date());
                    return taskStore.save(task);
                })
                .then();
    }

    private Mono<ExecuteCodeResult> executeCode(Task task) {
        var scriptOutput = new StringWriter();
        var engine = GraalJSScriptEngine.create(null, Context.newBuilder("js"));
        engine.getContext().setWriter(scriptOutput);
        return Mono.fromCallable(() -> engine.eval(task.getCode()))
                .then(Mono.fromCallable(() ->
                        ExecuteCodeResult.builder()
                                .output(scriptOutput.toString())
                                .build()
                ))
                .doOnError(err -> log.error("Error evaluating script {}:\n{}", task.getId(), err))
                .onErrorResume(err -> Mono.just(
                        ExecuteCodeResult.builder()
                                .output(scriptOutput.toString())
                                .error(err.getMessage())
                                .errored(true)
                                .build()
                ))
                .timeout(Duration.ofSeconds(20))
                .doOnError(err -> log.error("Script {} timed-out", task.getId()))
                .onErrorReturn(TimeoutException.class,
                        ExecuteCodeResult.builder()
                                .output(scriptOutput.toString())
                                .error("Script timed-out. Maximum execution duration is 20 seconds")
                                .errored(true)
                                .build()
                );
    }

    public Mono<Void> stopById(String taskId) {
        return taskStore.findById(taskId)
                .flatMap(task -> {
                    stopTaskInternal(task);
                    return taskStore.save(task);
                })
                .then();
    }

    public Mono<Void> stopAll() {
        return taskStore.findAllById(taskIdAndExecutionState.keySet())
                .collectList()
                .flatMapMany(executingTasks -> {
                    executingTasks.forEach(this::stopTaskInternal);
                    return taskStore.saveAll(executingTasks);
                })
                .then();
    }

    private void stopTaskInternal(Task task) {
        var taskExecutionState = taskIdAndExecutionState.get(task.getId());
        if (taskExecutionState != null) {
            taskExecutionState.getTaskExecution().dispose();
            taskIdAndExecutionState.remove(task.getId());
            task.setStatus(TaskStatus.STOPPED);
            task.setEndExecDate(new Date());
            task.setOutput(taskExecutionState.getOutput().toString());
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ExecuteCodeResult {
        private String output;
        private String error;
        private boolean errored;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ExecutionState {
        private Disposable taskExecution;
        private StringWriter output;
    }
}
