package pragmasoft.andriilupynos.js_executioner.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pragmasoft.andriilupynos.js_executioner.data.TaskStore;
import pragmasoft.andriilupynos.js_executioner.data.domain.Task;
import pragmasoft.andriilupynos.js_executioner.data.domain.TaskStatus;
import pragmasoft.andriilupynos.js_executioner.exception.TaskNotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;

@Service
public class TaskQueryService {

    @Autowired private TaskStore taskStore;
    @Autowired private TaskExecuteService taskExecuteService;

    /**
     * Returns information about the task, including current script output,
     * or Mono.error(TaskNotFoundException) if task with such id does not exist
     *
     * @param id task id
     * @return Mono with task information
     */
    public Mono<Task> getTaskWithCurrentOutputById(String id) {
        return taskStore.findById(id)
                .map(task -> {
                    if (task.getStatus() == TaskStatus.EXECUTING)
                        task.setOutput(taskExecuteService.getCurrentExecutionOutput(task.getId()));
                    return task;
                })
                .switchIfEmpty(Mono.error(new TaskNotFoundException("Task with such id does not exist")));
    }

    /**
     * Find all tasks. If status is provided then return only tasks
     * with given status. If newFirst is provided and it's true, then results
     * are returned from newest to oldest tasks, if it's false, then from
     * oldest to newest. If newFirst is not provided, then ordering is not guaranteed
     *
     * Note: ordering should be moved to DB level, but for simplicity I left it here.
     *
     * @param filter filter to apply to returned tasks, includes sorting
     * @return tasks matching given parameters
     */
    public Flux<Task> search(TaskSearchModel filter) {
        var findFlux = taskStore.findAll();
        if (filter.status != null)
            findFlux = findFlux.filter(task -> task.getStatus().equals(filter.status));
        if (filter.name != null)
            findFlux = findFlux.filter(task -> task.getName().equals(filter.name));
        if (filter.newFirst != null) {
            Comparator<Task> comparator;
            if (filter.newFirst)
                comparator = Comparator.comparing(Task::getCreatedDate).reversed();
            else
                comparator = Comparator.comparing(Task::getCreatedDate);
            findFlux = findFlux.sort(comparator);
        }
        return findFlux;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskSearchModel {
        private String name;
        private TaskStatus status;
        private Boolean newFirst;
    }

}
