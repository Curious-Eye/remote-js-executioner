package pragmasoft.andriilupynos.js_executioner.data.repository;

import pragmasoft.andriilupynos.js_executioner.data.domain.TaskEntity;
import reactor.core.publisher.Mono;

public interface TaskRepository {

    Mono<TaskEntity> findByName(String name);
    Mono<TaskEntity> save(TaskEntity task);
    Mono<Void> deleteAll();

}
