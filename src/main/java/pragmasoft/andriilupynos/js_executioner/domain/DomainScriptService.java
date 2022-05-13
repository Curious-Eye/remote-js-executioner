package pragmasoft.andriilupynos.js_executioner.domain;

import pragmasoft.andriilupynos.js_executioner.domain.exception.IllegalArgumentException;
import pragmasoft.andriilupynos.js_executioner.domain.exception.ScriptNotFoundException;
import pragmasoft.andriilupynos.js_executioner.domain.internal.ByteArrayScriptOutput;
import pragmasoft.andriilupynos.js_executioner.util.CurrentClock;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DomainScriptService implements ScriptService {

    private final ScriptFactory scriptFactory;
    private final ScriptInfoRepository scriptInfoRepository;
    private final ScriptExecutionRepository scriptExecutionRepository;
    private final ExecutorService executor;

    public DomainScriptService(
            ScriptFactory scriptFactory,
            ScriptInfoRepository scriptInfoRepository,
            ExecutorService executor,
            ScriptExecutionRepository scriptExecutionRepository
    ) {
        this.scriptFactory = scriptFactory;
        this.scriptInfoRepository = scriptInfoRepository;
        this.scriptExecutionRepository = scriptExecutionRepository;
        this.executor = executor;
    }

    @Override
    public ScriptInfo create(String code, String name) throws ScriptException {
        if (code == null)
            throw new IllegalArgumentException("code is required");

        ScriptInfo.ScriptOutput scriptOut = new ByteArrayScriptOutput();
        ScriptInfo.ScriptOutput scriptErr = new ByteArrayScriptOutput();
        var script = scriptFactory.createScript(code, scriptOut.asStream(), scriptErr.asStream());
        var scriptInfo = new ScriptInfo(
                script,
                name != null ? name : UUID.randomUUID().toString(),
                ScriptInfo.Status.SCHEDULED,
                Instant.now(CurrentClock.get()),
                scriptOut,
                scriptErr
        );
        scriptInfoRepository.store(scriptInfo);
        return scriptInfo;
    }

    @Override
    public ScriptExecution execute(ScriptInfo scriptInfo) throws ScriptException {
        var execution = new ScriptExecution(scriptInfo);
        this.executor.submit(execution);
        this.scriptExecutionRepository.store(execution);
        return execution;
    }

    @Override
    public ScriptInfo get(String name) {
        return Optional.ofNullable(this.scriptInfoRepository.get(name))
                .orElseThrow(() -> new ScriptNotFoundException(name));
    }

    @Override
    public Collection<ScriptInfo> all(SortBy by, ScriptInfo.Status status) throws ScriptException {
        return StreamSupport.stream(this.scriptInfoRepository.all(by).spliterator(), false)
                .filter(scriptInfo -> status == null || status == scriptInfo.getStatus())
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String name) {
        var script = this.get(name);
        if (!this.scriptInfoRepository.delete(script))
            throw new ScriptNotFoundException(name);
        try {
            this.stopExecution(name);
        } catch(ScriptException ignored) { /* Should be ignored */ }
    }

    @Override
    public Optional<ScriptExecution> executionOf(ScriptInfo scriptInfo) throws ScriptException {
        return Optional.ofNullable(this.scriptExecutionRepository.get(scriptInfo.name));
    }

    @Override
    public void stopExecution(String name) {
        var execution =
                Optional.ofNullable(this.scriptExecutionRepository.getAndRemove(name))
                        .orElseThrow(() -> new ScriptNotFoundException(name));

        execution.cancel(true);
    }
}
