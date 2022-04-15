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

    private final Scheduler scheduler = Schedulers.newParallel(TaskExecuteService.class.getName());
//            Schedulers.newBoundedElastic(4, 500, TaskExecuteService.class.getName());
    private final ConcurrentHashMap<String, Disposable> taskIdAndExecution = new ConcurrentHashMap<>();

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
                    taskIdAndExecution.put(
                            task.getId(),
                            scheduler.schedule(() -> this.execute(task).block())
                    );
                    log.debug("Scheduled task {}", task.getId());
                })
                .then()
                .block();
    }

    /**
     * Execute given task
     *
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

    private Mono<ExecuteCodeResult> executeCode(Task task) {
        var scriptOutput = new StringWriter();
        var engine = GraalJSScriptEngine.create(null, Context.newBuilder("js"));
        engine.getContext().setWriter(scriptOutput);
        return Mono.fromCallable(() -> engine.eval(task.getCode()))
                .then(Mono.fromCallable(() -> ExecuteCodeResult.builder().output(scriptOutput.toString()).build()))
                .timeout(Duration.ofSeconds(60))
                .doOnError(err -> log.error("Error evaluating script {}: {}", task.getId(), err.getMessage()))
                .onErrorResume(err -> Mono.just(buildExecResultOnError(scriptOutput, err)));
    }

    private ExecuteCodeResult buildExecResultOnError(StringWriter scriptOutput, Throwable err) {
        var resBuilder =
                ExecuteCodeResult.builder().output(scriptOutput.toString());

        if (err.getClass().isAssignableFrom(TimeoutException.class)) {
            return resBuilder.error("Script timed-out. Maximum execution duration is 60 seconds")
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
     * Stop task by id. This method does nothing if task with such id is not being executed
     *
     * @param taskId - Id of the task
     * @return - Mono signaling when operation completes
     */
    public Mono<Void> stopById(String taskId) {
        log.debug("Stopping task {}", taskId);
        return Mono.fromCallable(() -> {
            var taskExecutionState = taskIdAndExecution.get(taskId);
            if (taskExecutionState != null) {
                taskExecutionState.dispose();
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
            taskIdAndExecution.values().forEach(Disposable::dispose);
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

}
