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
import pragmasoft.andriilupynos.js_executioner.data.ScriptStore;
import pragmasoft.andriilupynos.js_executioner.data.domain.Script;
import pragmasoft.andriilupynos.js_executioner.data.domain.ScriptStatus;
import pragmasoft.andriilupynos.js_executioner.exception.IllegalArgumentException;
import pragmasoft.andriilupynos.js_executioner.exception.ScriptNotFoundException;
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
public class ScriptExecuteService {

    private final Logger log = LoggerFactory.getLogger(ScriptExecuteService.class);

    @Autowired private ScriptStore scriptStore;

    private final Scheduler scheduler =
            Schedulers.newParallel(ScriptExecuteService.class.getName());
    private final ConcurrentHashMap<String, ExecutionContext> scriptIdAndExecution = new ConcurrentHashMap<>();

    /**
     * Periodically schedules scripts for execution
     */
    @Scheduled(fixedRate = 2000)
    public void checkScriptsAndExecute() {
        scriptStore.findAllByStatusIn(List.of(ScriptStatus.EXECUTING, ScriptStatus.SCHEDULED, ScriptStatus.NEW))
                .filter(script -> !scriptIdAndExecution.containsKey(script.getId()))
                .filter(script -> script.getScheduledAt() == null || !script.getScheduledAt().after(new Date()))
                .flatMap(script -> {
                    script.setStatus(ScriptStatus.SCHEDULED);
                    return scriptStore.save(script);
                })
                .doOnNext(script -> {
                    var scriptOutputWriter = new StringWriter();
                    scriptIdAndExecution.put(
                            script.getId(),
                            ExecutionContext.builder()
                                    .execution(scheduler.schedule(() -> this.execute(script, scriptOutputWriter).subscribe()))
                                    .currentOutputWriter(scriptOutputWriter)
                                    .build()
                    );
                    log.debug("Scheduled script {} for execution", script.getId());
                })
                .then()
                .block();
    }

    /**
     * Execute given script and write its output to outputWriter
     *
     * @param script script to execute
     * @param outputWriter writer to write script's output to
     * @return Mono indicating when script is executed
     */
    public Mono<Void> execute(Script script, StringWriter outputWriter) {
        log.debug("Executing script {}", script.getId());
        script.setStatus(ScriptStatus.EXECUTING);
        script.setBeginExecDate(new Date());
        return scriptStore.save(script)
                .then(executeCode(script, outputWriter))
                .flatMap(executeRes -> {
                    script.setStatus(
                            executeRes.errored ? ScriptStatus.ERRORED :
                                    executeRes.stopped ? ScriptStatus.STOPPED : ScriptStatus.COMPLETED
                    );
                    script.setOutput(executeRes.getOutput());
                    script.setError(executeRes.getError());
                    script.setEndExecDate(new Date());
                    return scriptStore.save(script);
                })
                .doOnNext(it -> {
                    scriptIdAndExecution.remove(it.getId());
                    log.debug("Done executing script {}", it.getId());
                })
                .then();
    }

    private Mono<ExecuteCodeResult> executeCode(Script script, StringWriter outputWriter) {
        var engine = GraalJSScriptEngine.create(null, Context.newBuilder("js"));
        engine.getContext().setWriter(outputWriter);
        return Mono.fromCallable(() -> engine.eval(script.getCode()))
                .then(Mono.fromCallable(() ->
                        ExecuteCodeResult.builder().output(outputWriter.toString()).build()))
                .timeout(Duration.ofSeconds(30))
                .doOnError(err -> log.error("Error evaluating script {}: {}", script.getId(), err.getMessage()))
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
     * Returns current execution output for script with requested id if
     * it is currently being executed or null
     *
     * @param scriptId id of the script to return
     * @return Execution output or null in case script is not being executed
     */
    public String getCurrentExecutionOutput(String scriptId) {
        var exec = scriptIdAndExecution.get(scriptId);
        if (exec == null)
            return null;
        return exec.currentOutputWriter.toString();
    }

    /**
     * Stop script by id. This method does nothing if script with such id is not being executed.
     * Returns Mono.error(ScriptNotFoundException) if script with such id does not exist
     *
     * @param scriptId Id of the script
     * @return Mono signaling when operation completes
     */
    public Mono<Void> changeExecution(String scriptId, ChangeExecutionModel model) {
        log.debug("Stopping script {}", scriptId);
        if (model.getAction() != ChangeExecutionAction.STOP) {
            return Mono.error(
                    new IllegalArgumentException(
                            "Cannot change execution: Unexpected action passed - " + model.getAction()
                    )
            );
        }
        return scriptStore.findById(scriptId)
                .switchIfEmpty(Mono.error(new ScriptNotFoundException("Script with such id does not exist")))
                .mapNotNull(unused -> {
                    var scriptExecutionState = scriptIdAndExecution.get(scriptId);
                    if (scriptExecutionState != null) {
                        scriptExecutionState.getExecution().dispose();
                    }
                    return null;
                });
    }

    /**
     * Stop all current scripts.
     *
     * @return Mono signaling when operation completes
     */
    public Mono<Void> stopAll() {
        log.debug("Stopping all current scripts");
        return Mono.fromCallable(() -> {
            scriptIdAndExecution.values().forEach(exec -> exec.getExecution().dispose());
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
     * This class is needed to allow access to script's output during its execution
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
