package pragmasoft.andriilupynos.js_executioner.application.api.dto;

import lombok.Getter;
import pragmasoft.andriilupynos.js_executioner.domain.ScriptExecution;
import pragmasoft.andriilupynos.js_executioner.domain.ScriptInfo;

import java.util.Date;

@Getter
public class ScriptDto {

    private final String name;
    private final String code;
    private final Date createdDate;
    private final ScriptExecutionDto execution;

    public ScriptDto(ScriptInfo scriptInfo, ScriptExecution scriptExecution) {
        this.name = scriptInfo.name;
        this.code = scriptInfo.script.code;
        this.createdDate = new Date(scriptInfo.created.toEpochMilli());
        this.execution = new ScriptExecutionDto(scriptInfo, scriptExecution);
    }

}
