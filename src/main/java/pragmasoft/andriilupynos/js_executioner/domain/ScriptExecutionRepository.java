package pragmasoft.andriilupynos.js_executioner.domain;

public interface ScriptExecutionRepository {

    void store(ScriptExecution scriptExecution);

    ScriptExecution get(String name);

    ScriptExecution getAndRemove(String name);

}
