package pragmasoft.andriilupynos.js_executioner.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pragmasoft.andriilupynos.js_executioner.data.domain.Task;
import pragmasoft.andriilupynos.js_executioner.data.domain.TaskEntity;
import pragmasoft.andriilupynos.js_executioner.data.domain.TaskStatus;
import pragmasoft.andriilupynos.js_executioner.data.repository.TaskRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskStore {

    @Autowired private TaskRepository taskRepository;

    public Mono<Task> save(Task task) {
        return taskRepository.save(convertToTaskEntity(task))
                .map(TaskStore::convertToTask);
    }

    public Flux<Task> saveAll(Collection<Task> task) {
        return taskRepository.saveAll(
                        task.stream()
                                .map(TaskStore::convertToTaskEntity)
                                .collect(Collectors.toList())
                )
                .map(TaskStore::convertToTask);
    }

    public Mono<Void> deleteById(String id) {
        return taskRepository.deleteById(id);
    }

    public Mono<Void> deleteAll() {
        return taskRepository.deleteAll();
    }

    public Flux<Task> findAllByStatusIn(List<TaskStatus> statuses) {
        return taskRepository.findAllByStatusIn(statuses)
                .map(TaskStore::convertToTask);
    }

    public Mono<Task> findById(String taskId) {
        return taskRepository.findById(taskId)
                .map(TaskStore::convertToTask);
    }

    public Flux<Task> findAll() {
        return taskRepository.findAll()
                .map(TaskStore::convertToTask);
    }

    private static Task convertToTask(TaskEntity taskEntity) {
        return Task.builder()
                .id(taskEntity.getId())
                .name(taskEntity.getName())
                .code(taskEntity.getCode())
                .output(taskEntity.getOutput())
                .error(taskEntity.getError())
                .status(taskEntity.getStatus())
                .scheduledAt(taskEntity.getScheduledAt())
                .beginExecDate(taskEntity.getBeginExecDate())
                .endExecDate(taskEntity.getEndExecDate())
                .createdDate(taskEntity.getCreatedDate())
                .build();
    }

    private static TaskEntity convertToTaskEntity(Task task) {
        return TaskEntity.builder()
                .id(task.getId())
                .name(task.getName())
                .code(task.getCode())
                .output(task.getOutput())
                .error(task.getError())
                .status(task.getStatus())
                .scheduledAt(task.getScheduledAt())
                .beginExecDate(task.getBeginExecDate())
                .endExecDate(task.getEndExecDate())
                .createdDate(task.getCreatedDate())
                .build();
    }

}
