package pragmasoft.andriilupynos.js_executioner.application.api.dto;

import lombok.Getter;
import pragmasoft.andriilupynos.js_executioner.domain.model.script.Script;

import java.util.Date;

@Getter
public class ScriptDto {

    private final String id;
    private final String code;
    private final Date createdDate;
    private final ScriptExecutionDto execution;

    public ScriptDto(Script script) {
        this.id = script.getId();
        this.code = script.getCode();
        this.createdDate = script.getCreatedDate();
        this.execution = new ScriptExecutionDto(script.getExecutionInfo());
    }

}
