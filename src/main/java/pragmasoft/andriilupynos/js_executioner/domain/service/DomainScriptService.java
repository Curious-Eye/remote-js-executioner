package pragmasoft.andriilupynos.js_executioner.domain.service;

import org.graalvm.polyglot.Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pragmasoft.andriilupynos.js_executioner.domain.model.exception.IllegalArgumentException;
import pragmasoft.andriilupynos.js_executioner.domain.model.exception.ScriptNotFoundException;
import pragmasoft.andriilupynos.js_executioner.domain.model.script.*;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public class DomainScriptService implements ScriptService {

    private final Logger log = LoggerFactory.getLogger(DomainScriptService.class);

    private final Engine engine;
    private final ScheduledExecutorService executor;
    private final ConcurrentMap<String, Script> scripts = new ConcurrentHashMap<>();

    public DomainScriptService(Engine engine, ScheduledExecutorService executor) {
        this.engine = engine;
        this.executor = executor;
    }

    @Override
    public String scheduleScript(String code, Date scheduledAt) {
        log.debug("Scheduling script for future execution with code: {}", code);
        var script = new Script(UUID.randomUUID().toString(), code, scheduledAt);
        script.validateCode(this.engine);
        this.scripts.put(script.getId(), script);
        script.enqueueExecution(this.engine, this.executor);
        return script.getId();
    }

    @Override
    public Script getFullInfoById(String id) {
        log.debug("Querying full script info by id {}", id);
        var script = this.scripts.get(id);
        if (script == null)
            throw new ScriptNotFoundException(id);

        script.syncOutput();
        return new Script(script);
    }

    @Override
    public List<ScriptShortInfo> getShortInfoMatching(ScriptFindFilter filter) {
        log.debug("Querying scripts matching filter {}", filter);
        var scriptsToReturn = scripts.values()
                .stream()
                .filter(script ->
                    filter.getStatus() == null || script.getExecutionInfo().getStatus().equals(filter.getStatus())
                )
                .map(ScriptShortInfo::new)
                .collect(Collectors.toList());
        if (filter.getNewFirst() != null) {
            Comparator<ScriptShortInfo> comparator;
            if (Boolean.TRUE.equals(filter.getNewFirst()))
                comparator = Comparator.comparing(ScriptShortInfo::getCreatedDate).reversed();
            else
                comparator = Comparator.comparing(ScriptShortInfo::getCreatedDate);
            scriptsToReturn.sort(comparator);
        }
        return scriptsToReturn;
    }

    @Override
    public Execution getExecutionInfo(String id) {
        log.debug("Querying script's execution with id {}", id);
        var script = this.scripts.get(id);
        if (script == null)
            throw new ScriptNotFoundException(id);

        return script.getExecutionInfo();
    }

    @Override
    public Script changeExecution(String id, ExecutionStatus status) {
        log.debug("Changing script's execution with id {}. status = {}", id, status);
        var script = this.scripts.get(id);
        if (script == null)
            throw new ScriptNotFoundException(id);

        if (status != ExecutionStatus.STOPPED)
            throw new IllegalArgumentException("Illegal status change requested - " + status +
                    ". Status can only be changed to STOPPED");

        script.stopExecution();
        return new Script(script);
    }

    @Override
    public void deleteById(String id) {
        log.debug("Deleting script by id {}", id);
        var script = this.scripts.remove(id);
        if (script == null)
            throw new ScriptNotFoundException(id);
        else
            script.stopExecution();
    }

    @Override
    // This method is currently only used in tests, but might be exposed via REST API in the future
    @SuppressWarnings("unused")
    public void deleteAll() {
        log.debug("Deleting all scripts");
        this.scripts.values().forEach(Script::stopExecution);
        this.scripts.clear();
    }

}
