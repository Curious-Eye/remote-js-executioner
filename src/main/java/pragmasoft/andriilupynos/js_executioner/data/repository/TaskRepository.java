package pragmasoft.andriilupynos.js_executioner.data.repository;

import pragmasoft.andriilupynos.js_executioner.data.domain.TaskEntity;
import pragmasoft.andriilupynos.js_executioner.data.domain.TaskStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TaskRepository {

    Mono<TaskEntity> findByName(String name);
    Mono<TaskEntity> save(TaskEntity task);
    Mono<Void> deleteAll();
    Flux<TaskEntity> findAllByStatusIn(List<TaskStatus> statuses);

}
