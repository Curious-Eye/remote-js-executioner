package pragmasoft.andriilupynos.js_executioner.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pragmasoft.andriilupynos.js_executioner.data.TaskStore;
import pragmasoft.andriilupynos.js_executioner.exception.TaskNotFoundException;
import reactor.core.publisher.Mono;

@Service
public class TaskDeleteService {

    private final Logger log = LoggerFactory.getLogger(TaskDeleteService.class);

    @Autowired private TaskExecuteService taskExecuteService;
    @Autowired private TaskStore taskStore;

    /**
     * Delete a task by id.
     * Return Mono.error(TaskNotFoundException) if task with such id does not exist
     *
     * @param id id of a task
     * @return Mono signaling when operation is complete
     */
    public Mono<Void> deleteById(String id) {
        log.debug("User deletes task with id {}", id);
        return taskStore.findById(id)
                .switchIfEmpty(Mono.error(new TaskNotFoundException("Task with such id does not exist")))
                .then(
                        taskExecuteService.changeExecution(
                                        id,
                                        TaskExecuteService.ChangeExecutionModel.builder()
                                                .action(TaskExecuteService.ChangeExecutionAction.STOP)
                                                .build()
                                )
                                .thenReturn(1)
                                .onErrorReturn(1)
                )
                .then(taskStore.deleteById(id))
                .then();
    }

}
