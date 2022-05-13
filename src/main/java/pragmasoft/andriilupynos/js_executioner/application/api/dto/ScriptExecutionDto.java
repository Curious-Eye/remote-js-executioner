package pragmasoft.andriilupynos.js_executioner.application.api.dto;

import lombok.Getter;
import pragmasoft.andriilupynos.js_executioner.domain.ScriptExecution;
import pragmasoft.andriilupynos.js_executioner.domain.ScriptInfo;

import java.time.Duration;
import java.util.Date;

@Getter
public class ScriptExecutionDto {

    private final String output;
    private final String error;
    private final ScriptExecutionStatusDto status;
    private final Date beginExecDate;
    private final Date endExecDate;
    private final Long executionDurationMillis;

    public ScriptExecutionDto(ScriptInfo scriptInfo, ScriptExecution execution) {
        this.output = scriptInfo.getOut();
        this.error = scriptInfo.getErr();
        this.status = ScriptExecutionStatusDto.valueOf(scriptInfo.getStatus().name());
        if (execution != null) {
            this.beginExecDate = execution.getStarted().map(instant -> new Date(instant.toEpochMilli())).orElse(null);
            this.endExecDate = execution.getFinished().map(instant -> new Date(instant.toEpochMilli())).orElse(null);
            this.executionDurationMillis = execution.getDuration().map(Duration::toMillis).orElse(null);
        } else {
            this.beginExecDate = null;
            this.endExecDate = null;
            this.executionDurationMillis = null;
        }
    }

}
