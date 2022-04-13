package pragmasoft.andriilupynos.js_executioner.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pragmasoft.andriilupynos.js_executioner.data.domain.Task;
import pragmasoft.andriilupynos.js_executioner.data.domain.TaskEntity;
import pragmasoft.andriilupynos.js_executioner.data.repository.TaskRepository;
import reactor.core.publisher.Mono;

@Service
public class TaskStore {

    @Autowired private TaskRepository taskRepository;

    public Mono<Task> findByName(String name) {
        return taskRepository.findByName(name)
                .map(TaskStore::convertToTask);
    }

    public Mono<Task> save(Task task) {
        return taskRepository.save(convertToTaskEntity(task))
                .map(TaskStore::convertToTask);
    }

    private static Task convertToTask(TaskEntity taskEntity) {
        return Task.builder()
                .id(taskEntity.getId())
                .name(taskEntity.getName())
                .code(taskEntity.getCode())
                .status(taskEntity.getStatus())
                .build();
    }

    private static TaskEntity convertToTaskEntity(Task task) {
        return TaskEntity.builder()
                .id(task.getId())
                .name(task.getName())
                .code(task.getCode())
                .status(task.getStatus())
                .build();
    }

}
