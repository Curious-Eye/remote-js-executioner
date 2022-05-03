package pragmasoft.andriilupynos.js_executioner.data.repository.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import pragmasoft.andriilupynos.js_executioner.data.domain.ScriptEntity;
import pragmasoft.andriilupynos.js_executioner.data.domain.ScriptStatus;
import pragmasoft.andriilupynos.js_executioner.data.repository.ScriptRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConcurrentInMemoryScriptRepository implements ScriptRepository {

    private final ConcurrentHashMap<String, ScriptEntity> scriptStore = new ConcurrentHashMap<>();

    @Override
    public Flux<ScriptEntity> findAllByStatusIn(List<ScriptStatus> statuses) {
        return Flux.fromStream(scriptStore.values().parallelStream())
                .filter(script -> statuses.contains(script.getStatus()));
    }

    @Override
    public Mono<ScriptEntity> save(ScriptEntity script) {
        return Mono.fromCallable(() -> {
            if (!StringUtils.hasText(script.getId()))
                script.setId(UUID.randomUUID().toString());

            scriptStore.put(script.getId(), script);
            return script;
        });
    }

    @Override
    public Flux<ScriptEntity> saveAll(Collection<ScriptEntity> script) {
        return Flux.fromIterable(script)
                .flatMap(this::save);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return Mono.fromCallable(() -> {
            scriptStore.remove(id);
            return null;
        });
    }

    @Override
    public Mono<Void> deleteAll() {
        return Mono.fromCallable(() -> {
            scriptStore.clear();
            return null;
        });
    }

    @Override
    public Mono<ScriptEntity> findById(String id) {
        return Mono.fromCallable(() -> scriptStore.get(id));
    }

    @Override
    public Flux<ScriptEntity> findAll() {
        return Flux.fromIterable(scriptStore.values());
    }

}
