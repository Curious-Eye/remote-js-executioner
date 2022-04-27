package pragmasoft.andriilupynos.js_executioner.data.repository.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import pragmasoft.andriilupynos.js_executioner.data.domain.TaskEntity;
import pragmasoft.andriilupynos.js_executioner.data.domain.TaskStatus;
import pragmasoft.andriilupynos.js_executioner.data.repository.TaskRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConcurrentInMemoryTaskRepository implements TaskRepository {

    private final ConcurrentHashMap<String, TaskEntity> taskStore = new ConcurrentHashMap<>();

    @Override
    public Flux<TaskEntity> findAllByStatusIn(List<TaskStatus> statuses) {
        return Flux.fromStream(taskStore.values().parallelStream())
                .filter(task -> statuses.contains(task.getStatus()));
    }

    @Override
    public Mono<TaskEntity> save(TaskEntity task) {
        return Mono.fromCallable(() -> {
            if (!StringUtils.hasText(task.getId()))
                task.setId(UUID.randomUUID().toString());

            taskStore.put(task.getId(), task);
            return task;
        });
    }

    @Override
    public Flux<TaskEntity> saveAll(Collection<TaskEntity> task) {
        return Flux.fromIterable(task)
                .flatMap(this::save);
    }

    @Override
    public Mono<Void> deleteAll() {
        return Mono.fromCallable(() -> {
            taskStore.clear();
            return null;
        });
    }

    @Override
    public Mono<TaskEntity> findById(String id) {
        return Mono.fromCallable(() -> taskStore.get(id));
    }

    @Override
    public Flux<TaskEntity> findAll() {
        return Flux.fromIterable(taskStore.values());
    }

}
