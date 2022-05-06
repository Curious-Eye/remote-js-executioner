package pragmasoft.andriilupynos.js_executioner.application.api.dto;

import lombok.Getter;
import pragmasoft.andriilupynos.js_executioner.domain.model.script.Execution;

import java.util.Date;

@Getter
public class ScriptExecutionDto {

    private final String output;
    private final String error;
    private final String interruptionMsg;
    private final ScriptExecutionStatusDto status;
    private final Date scheduledAt;
    private final Date beginExecDate;
    private final Date endExecDate;
    private final Long executionDurationMillis;

    public ScriptExecutionDto(Execution execution) {
        this.output = execution.getOutput();
        this.error = execution.getError();
        this.interruptionMsg = execution.getInterruptionMsg();
        this.status = ScriptExecutionStatusDto.valueOf(execution.getStatus().name());
        this.scheduledAt = execution.getScheduledAt();
        this.beginExecDate = execution.getBeginExecDate();
        this.endExecDate = execution.getEndExecDate();
        this.executionDurationMillis = execution.getExecutionDurationMillis();
    }

}
