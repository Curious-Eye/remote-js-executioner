package pragmasoft.andriilupynos.js_executioner.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pragmasoft.andriilupynos.js_executioner.data.TaskStore;
import pragmasoft.andriilupynos.js_executioner.data.domain.Task;
import pragmasoft.andriilupynos.js_executioner.data.domain.TaskStatus;
import reactor.core.publisher.Mono;

@Service
public class TaskQueryService {

    @Autowired private TaskStore taskStore;
    @Autowired private TaskExecuteService taskExecuteService;

    /**
     * Returned information about the task, including current script output
     * @param id task id
     * @return Mono with task information
     */
    public Mono<Task> getTaskWithCurrentOutputById(String id) {
        return taskStore.findById(id)
                .map(task -> {
                    if (task.getStatus() == TaskStatus.EXECUTING)
                        task.setOutput(taskExecuteService.getCurrentExecutionOutput(task.getId()));
                    return task;
                });
    }

}
