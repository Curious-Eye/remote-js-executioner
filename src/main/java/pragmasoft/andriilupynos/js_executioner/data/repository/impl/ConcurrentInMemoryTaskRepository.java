package pragmasoft.andriilupynos.js_executioner.data.repository.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import pragmasoft.andriilupynos.js_executioner.data.domain.TaskEntity;
import pragmasoft.andriilupynos.js_executioner.data.repository.TaskRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConcurrentInMemoryTaskRepository implements TaskRepository {

    private final ConcurrentHashMap<String, TaskEntity> taskStore = new ConcurrentHashMap<>();

    @Override
    public Mono<TaskEntity> findByName(String name) {
        return Flux.fromStream(taskStore.values().stream())
                .filter(it -> it.getName().equals(name))
                .next();
    }

    @Override
    public Mono<TaskEntity> save(TaskEntity task) {
        if (!StringUtils.hasText(task.getId()))
            task.setId(UUID.randomUUID().toString());

        taskStore.put(task.getId(), task);
        return Mono.just(task);
    }

    @Override
    public Mono<Void> deleteAll() {
        taskStore.clear();
        return Mono.empty();
    }

}
