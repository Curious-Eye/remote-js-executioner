package pragmasoft.andriilupynos.js_executioner.application.api.dto;

import lombok.Getter;
import pragmasoft.andriilupynos.js_executioner.domain.ScriptInfo;

import java.util.Date;

@Getter
public class ScriptSimpleDto {

    private final String name;
    private final String code;
    private final Date createdDate;
    private final ScriptExecutionStatusDto status;

    public ScriptSimpleDto(ScriptInfo script) {
        this.name = script.name;
        this.code = script.script.code;
        this.createdDate = new Date(script.created.toEpochMilli());
        this.status = ScriptExecutionStatusDto.valueOf(script.getStatus().name());
    }

}
