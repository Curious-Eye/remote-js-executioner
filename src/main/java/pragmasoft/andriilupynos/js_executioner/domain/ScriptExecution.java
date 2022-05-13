package pragmasoft.andriilupynos.js_executioner.domain;

import pragmasoft.andriilupynos.js_executioner.util.CurrentClock;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ScriptExecution implements RunnableFuture<Void> {

    protected final ScriptInfo scriptInfo;
    Instant started = null;
    Instant finished = null;

    private final FutureTask<Void> execution;

    public ScriptExecution(ScriptInfo scriptInfo) {
        this.execution = new FutureTask<>(this::exec, null);
        this.scriptInfo = scriptInfo;
    }

    private void exec() {
        try {
            this.started = Instant.now(CurrentClock.get());
            this.scriptInfo.setStatus(ScriptInfo.Status.RUNNING);
            this.scriptInfo.script.run();
            this.scriptInfo.setStatus(ScriptInfo.Status.COMPLETED);
        } catch (Exception e) {
            this.scriptInfo.setStatus(ScriptInfo.Status.ERROR);
            this.writeScriptErrorWithStackTrace(e);
        } finally {
            this.finished = Instant.now(CurrentClock.get());
        }
    }

    private void writeScriptErrorWithStackTrace(Exception e) {
        try {
            this.scriptInfo.script.err.write((e.getMessage() + "\n").getBytes(StandardCharsets.UTF_8));
            var jsStackTrace = Arrays.stream(e.getStackTrace())
                    .filter(it -> it.getClassName().equals("<js>"))
                    .map(StackTraceElement::toString)
                    .collect(Collectors.toList());

            if (!jsStackTrace.isEmpty()) {
                this.scriptInfo.script.err.write("Stack trace:\n".getBytes(StandardCharsets.UTF_8));

                for (String stackTraceEl : jsStackTrace)
                    this.scriptInfo.script.err.write((stackTraceEl + "\n").getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Optional<Instant> getStarted() {
        return Optional.ofNullable(this.started);
    }

    public Optional<Instant> getFinished() {
        return Optional.ofNullable(this.finished);
    }

    public Optional<Duration> getDuration() {
        if (this.started != null && this.finished != null)
            return Optional.of(Duration.between(this.started, this.finished));

        return Optional.empty();
    }

    ScriptInfo.Status getStatus() {
        return scriptInfo.getStatus();
    }

    @Override
    public void run() {
        this.execution.run();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return this.execution.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return this.execution.isCancelled();
    }

    @Override
    public boolean isDone() {
        return this.execution.isDone();
    }

    @Override
    public Void get() throws InterruptedException, ExecutionException {
        return this.execution.get();
    }

    @Override
    public Void get(long timeout, @Nonnull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.execution.get(timeout, unit);
    }

    public String getScriptName() {
        return this.scriptInfo.name;
    }

}
