package pragmasoft.andriilupynos.js_executioner.infrastructure;

import pragmasoft.andriilupynos.js_executioner.domain.ScriptExecution;
import pragmasoft.andriilupynos.js_executioner.domain.ScriptExecutionRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryScriptExecutionRepository implements ScriptExecutionRepository {

    private final Map<String, ScriptExecution> executions = new ConcurrentHashMap<>();

    @Override
    public void store(ScriptExecution scriptExecution) {
        this.executions.put(scriptExecution.getScriptName(), scriptExecution);
    }

    @Override
    public ScriptExecution get(String name) {
        return this.executions.get(name);
    }

    @Override
    public ScriptExecution getAndRemove(String name) {
        return this.executions.remove(name);
    }
}
