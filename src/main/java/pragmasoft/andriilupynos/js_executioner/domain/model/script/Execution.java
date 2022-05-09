package pragmasoft.andriilupynos.js_executioner.domain.model.script;

import pragmasoft.andriilupynos.js_executioner.domain.model.exception.InvalidExecutionStateException;
import pragmasoft.andriilupynos.js_executioner.util.DateUtils;

import java.util.Date;
import java.util.Objects;

public class Execution {

    private final String scriptId;
    private final Date scheduledAt;
    private String output;
    private String error;
    private String interruptionMsg;
    private ExecutionStatus status;
    private Date beginExecDate;
    private Date endExecDate;

    public Execution(String scriptId, Date scheduledAt) {
        Objects.requireNonNull(scriptId, "scriptId cannot be null");
        this.scriptId = scriptId;
        this.scheduledAt = DateUtils.copyOrNull(scheduledAt);
        this.status = ExecutionStatus.NEW;
    }

    public Execution(Execution src) {
        this.scriptId = src.scriptId;
        this.scheduledAt = DateUtils.copyOrNull(src.scheduledAt);
        this.output = src.output;
        this.error = src.error;
        this.interruptionMsg = src.interruptionMsg;
        this.status = ExecutionStatus.valueOf(src.status.name());
        this.beginExecDate = DateUtils.copyOrNull(src.beginExecDate);
        this.endExecDate = DateUtils.copyOrNull(src.endExecDate);
    }

    public synchronized void setScheduled() {
        if (this.status != ExecutionStatus.NEW)
            throw new InvalidExecutionStateException(this.scriptId, "schedule", this.status, ExecutionStatus.NEW);

        this.status = ExecutionStatus.SCHEDULED;
    }

    public synchronized void setStarted() {
        if (this.status != ExecutionStatus.SCHEDULED)
            throw new InvalidExecutionStateException(this.scriptId, "start", this.status, ExecutionStatus.SCHEDULED);

        beginExecDate = new Date();
        this.status = ExecutionStatus.EXECUTING;
    }

    public synchronized void setCompleted(String output, String error) {
        if (this.status != ExecutionStatus.EXECUTING)
            throw new InvalidExecutionStateException(this.scriptId, "complete", this.status, ExecutionStatus.EXECUTING);

        endExecutionWith(output, error, ExecutionStatus.COMPLETED);
    }

    public synchronized void setErrored(String output, String error, String interruptionMsg) {
        if (this.status != ExecutionStatus.EXECUTING)
            throw new InvalidExecutionStateException(this.scriptId, "set errored", this.status, ExecutionStatus.EXECUTING);

        this.interruptionMsg = interruptionMsg;
        endExecutionWith(output, error, ExecutionStatus.ERRORED);
    }

    public synchronized void setStopped(String output, String error) {
        if (this.status != ExecutionStatus.EXECUTING)
            throw new InvalidExecutionStateException(this.scriptId, "stop", this.status, ExecutionStatus.EXECUTING);

        endExecutionWith(output, error, ExecutionStatus.STOPPED);
    }

    private void endExecutionWith(String output, String error, ExecutionStatus status) {
        this.output = output;
        this.error = error;
        this.status = status;
        this.endExecDate = new Date();
    }

    protected synchronized void syncOutput(String output, String error) {
        if (status == ExecutionStatus.EXECUTING) {
            this.output = output;
            this.error = error;
        }
    }

    public long getDelayBeforeExecutionMillis() {
        if (this.scheduledAt == null)
            return 0;

        return this.scheduledAt.getTime() - new Date().getTime();
    }

    public Date getScheduledAt() {
        return scheduledAt;
    }

    public String getOutput() {
        return output;
    }

    public String getError() {
        return error;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public Date getBeginExecDate() {
        return beginExecDate;
    }

    public Date getEndExecDate() {
        return endExecDate;
    }

    public long getExecutionDurationMillis() {
        if (beginExecDate != null) {
            return Objects.requireNonNullElseGet(endExecDate, Date::new).getTime() - beginExecDate.getTime();
        } else {
            return 0L;
        }
    }

    public String getInterruptionMsg() {
        return interruptionMsg;
    }

}
