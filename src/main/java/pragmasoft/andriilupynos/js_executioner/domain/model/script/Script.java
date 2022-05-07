package pragmasoft.andriilupynos.js_executioner.domain.model.script;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.PolyglotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pragmasoft.andriilupynos.js_executioner.domain.model.exception.InvalidJSProvidedException;
import pragmasoft.andriilupynos.js_executioner.util.DateUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Representation of a script that can be executed.
 */
public class Script {

    private static final Logger log = LoggerFactory.getLogger(Script.class);

    private final String id;
    private final String code;
    private final Date createdDate;
    private final Execution execution;

    private OutputStream outStream;
    private OutputStream errStream;
    private ScheduledFuture<Execution> executionFuture;

    public Script(String id, String code, Date scheduledAt) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(code, "code must not be null");
        this.id = id;
        this.code = code;
        this.execution = new Execution(id, scheduledAt);
        this.createdDate = new Date();
    }

    public Script(Script src) {
        this.id = src.id;
        this.code = src.code;
        this.createdDate = DateUtils.copyOrNull(src.createdDate);
        this.execution = new Execution(src.getExecutionInfo());
        this.outStream = null;
        this.errStream = null;
        this.executionFuture = null;
    }

    /**
     * @param engine engine to build context to validate the code with
     */
    public void validateCode(Engine engine) {
        Context ctx = newContextBuilder().engine(engine).build();
        try {
            ctx.enter();
            ctx.parse("js", this.code);
        } catch (Exception e) {
            throw new InvalidJSProvidedException(e.getMessage());
        } finally {
            ctx.leave();
            ctx.close();
        }
    }

    /**
     * Enqueue script for execution.
     *
     * @param engine   engine to execute this script with
     * @param executor executor for execution scheduling
     */
    public void enqueueExecution(Engine engine, ScheduledExecutorService executor) {
        synchronized (this.execution) {
            this.execution.setScheduled();
            this.executionFuture =
                    executor.schedule(
                            this.execute(engine),
                            this.execution.getDelayBeforeExecutionMillis(),
                            TimeUnit.MILLISECONDS
                    );
        }
    }

    /**
     * Parallel execution is done according to this example:
     * https://github.com/oracle/graaljs/blob/master/graal-js/src/com.oracle.truffle.js.test.threading/src/com/oracle/truffle/js/test/threading/ExecutorsTest.java
     */
    private Callable<Execution> execute(Engine engine) {
        this.outStream = new ByteArrayOutputStream();
        this.errStream = new ByteArrayOutputStream();
        final ThreadLocal<Context> tl = ThreadLocal.withInitial(
                () -> newContextBuilder()
                        .engine(engine)
                        .out(this.outStream)
                        .err(this.errStream)
                        .build()
        );
        return () -> {
            log.debug("Began script execution {}", this.id);
            this.execution.setStarted();
            Context cx = tl.get();
            cx.enter();
            try {
                cx.eval("js", this.code);
                this.execution.setCompleted(this.outStream.toString(), this.errStream.toString());
            } catch (PolyglotException e) {
                if (e.getMessage().equals("Thread was interrupted.")) {
                    this.execution.setStopped(this.outStream.toString(), this.errStream.toString());
                } else {
                    this.execution.setErrored(
                            this.outStream.toString(),
                            this.errStream.toString(),
                            e.getMessage()
                    );
                }
            } finally {
                cx.leave();
                cx.close();
            }
            var exec = getExecutionInfo();
            log.debug("Finished script execution {} with status {}", this.id, exec.getStatus());
            return exec;
        };
    }

    /**
     * Stop execution of the current script if it is being performed
     */
    public void stopExecution() {
        synchronized (this.execution) {
            if (this.execution.getStatus() == ExecutionStatus.EXECUTING && this.executionFuture != null) {
                this.executionFuture.cancel(true);
                this.executionFuture = null;
            }
        }
    }

    public void syncOutput() {
        synchronized (this.execution) {
            if (this.execution.getStatus() == ExecutionStatus.EXECUTING) {
                this.execution.syncOutput(
                        this.outStream.toString(),
                        this.errStream.toString()
                );
            }
        }
    }

    public Execution getExecutionInfo() {
        synchronized (this.execution) {
            return new Execution(this.execution);
        }
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Date getCreatedDate() {
        return new Date(this.createdDate.getTime());
    }

    private static Context.Builder newContextBuilder() {
        return Context.newBuilder("js");
    }

}
