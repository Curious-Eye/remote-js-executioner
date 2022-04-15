package pragmasoft.andriilupynos.js_executioner.data.repository;

import pragmasoft.andriilupynos.js_executioner.data.domain.TaskEntity;
import pragmasoft.andriilupynos.js_executioner.data.domain.TaskStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TaskRepository {

    Mono<TaskEntity> findById(String id);
    Flux<TaskEntity> findAllById(Iterable<String> ids);
    Mono<TaskEntity> save(TaskEntity task);
    Flux<TaskEntity> saveAll(Iterable<TaskEntity> tasks);
    Mono<Void> deleteAll();
    Mono<TaskEntity> findByName(String name);
    Flux<TaskEntity> findAllByStatusIn(List<TaskStatus> statuses);

}
