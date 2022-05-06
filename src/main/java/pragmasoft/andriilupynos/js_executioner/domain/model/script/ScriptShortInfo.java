package pragmasoft.andriilupynos.js_executioner.domain.model.script;

import lombok.Getter;
import pragmasoft.andriilupynos.js_executioner.util.DateUtils;

import java.util.Date;

@Getter
public class ScriptShortInfo {

    private final String id;
    private final String code;
    private final Date scheduledAt;
    private final Date createdDate;
    private final ExecutionStatus status;

    public ScriptShortInfo(Script script) {
        this.id = script.getId();
        this.code = script.getCode();
        this.scheduledAt = DateUtils.copyOrNull(script.getExecutionInfo().getScheduledAt());
        this.createdDate = DateUtils.copyOrNull(script.getCreatedDate());
        this.status = script.getExecutionInfo().getStatus();
    }

}
