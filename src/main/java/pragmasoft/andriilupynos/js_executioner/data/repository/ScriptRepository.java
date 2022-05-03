package pragmasoft.andriilupynos.js_executioner.data.repository;

import pragmasoft.andriilupynos.js_executioner.data.domain.ScriptEntity;
import pragmasoft.andriilupynos.js_executioner.data.domain.ScriptStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

public interface ScriptRepository {

    Mono<ScriptEntity> findById(String id);
    Flux<ScriptEntity> findAll();
    Mono<ScriptEntity> save(ScriptEntity script);
    Flux<ScriptEntity> saveAll(Collection<ScriptEntity> script);
    Mono<Void> deleteById(String id);
    Mono<Void> deleteAll();
    Flux<ScriptEntity> findAllByStatusIn(List<ScriptStatus> statuses);

}
