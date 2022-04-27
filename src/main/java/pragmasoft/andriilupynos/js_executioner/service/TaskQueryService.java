package pragmasoft.andriilupynos.js_executioner.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pragmasoft.andriilupynos.js_executioner.api.dto.TaskStatusDto;
import pragmasoft.andriilupynos.js_executioner.data.TaskStore;
import pragmasoft.andriilupynos.js_executioner.data.domain.Task;
import pragmasoft.andriilupynos.js_executioner.data.domain.TaskStatus;
import pragmasoft.andriilupynos.js_executioner.exception.TaskNotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

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
     * @param status status of the tasks to return
     * @param newFirst whether to sort return tasks by creation date
     * @return tasks matching given parameters
     */
    public Flux<Task> getTasksMatching(TaskStatusDto status, Boolean newFirst) {
        var findFlux =
                status == null ?
                        taskStore.findAll() :
                        taskStore.findAllByStatusIn(List.of(TaskStatus.valueOf(status.name())));
        if (newFirst != null) {
            Comparator<Task> comparator;
            if (newFirst)
                comparator = Comparator.comparing(Task::getCreatedDate).reversed();
            else
                comparator = Comparator.comparing(Task::getCreatedDate);
            findFlux = findFlux.sort(comparator);
        }
        return findFlux;
    }
}
