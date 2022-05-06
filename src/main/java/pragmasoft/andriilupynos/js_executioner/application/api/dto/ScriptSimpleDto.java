package pragmasoft.andriilupynos.js_executioner.application.api.dto;

import lombok.Getter;
import pragmasoft.andriilupynos.js_executioner.domain.model.script.ScriptShortInfo;

import java.util.Date;

@Getter
public class ScriptSimpleDto {

    private final String id;
    private final String code;
    private final Date createdDate;
    private final Date scheduledAt;
    private final ScriptExecutionStatusDto status;

    public ScriptSimpleDto(ScriptShortInfo script) {
        this.id = script.getId();
        this.code = script.getCode();
        this.createdDate = script.getCreatedDate();
        this.scheduledAt = script.getScheduledAt();
        this.status = ScriptExecutionStatusDto.valueOf(script.getStatus().name());
    }

}
