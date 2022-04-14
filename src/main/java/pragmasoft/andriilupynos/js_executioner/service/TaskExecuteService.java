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
import reactor.core.publisher.Mono;

import java.io.StringWriter;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeoutException;

@Service
public class TaskExecuteService {

    private final Logger log = LoggerFactory.getLogger(TaskExecuteService.class);

    @Autowired private TaskStore taskStore;

    @Scheduled(initialDelay = 5000, fixedRate = 2000)
    private void checkTasksAndExecute() {

    }

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

    private Mono<ExecuteCodeState> executeCode(Task task) {
        var scriptOutput = new StringWriter();
        var engine = GraalJSScriptEngine.create(
                null,
                Context.newBuilder("js")
                        .option("js.ecmascript-version", "2020")
                        .option("js.script-engine-global-scope-import", "false")
        );
        engine.getContext().setWriter(scriptOutput);
        return Mono.fromCallable(() -> engine.eval(task.getCode()))
                .then(Mono.fromCallable(() ->
                        ExecuteCodeState.builder()
                                .output(scriptOutput.toString())
                                .build()
                ))
                .doOnError(err -> log.error("Error evaluating script {}:\n{}", task.getId(), err))
                .onErrorResume(err -> Mono.just(
                        ExecuteCodeState.builder()
                                .output(scriptOutput.toString())
                                .error(err.getMessage())
                                .errored(true)
                                .build()
                ))
                .timeout(Duration.ofSeconds(20))
                .doOnError(err -> log.error("Script {} timed-out", task.getId()))
                .onErrorReturn(TimeoutException.class,
                        ExecuteCodeState.builder()
                                .output(scriptOutput.toString())
                                .error("Script timed-out. Maximum execution duration is 20 seconds")
                                .errored(true)
                                .build()
                );
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ExecuteCodeState {
        private String output;
        private String error;
        private boolean errored;
    }
}
