package pragmasoft.andriilupynos.js_executioner.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pragmasoft.andriilupynos.js_executioner.api.dto.TaskCreateRespDto;
import pragmasoft.andriilupynos.js_executioner.api.dto.TaskCreateRqDto;
import pragmasoft.andriilupynos.js_executioner.api.dto.TaskDto;
import pragmasoft.andriilupynos.js_executioner.api.dto.TaskStatusDto;
import pragmasoft.andriilupynos.js_executioner.data.TaskStore;
import pragmasoft.andriilupynos.js_executioner.data.domain.Task;
import pragmasoft.andriilupynos.js_executioner.service.TaskExecuteService;
import pragmasoft.andriilupynos.js_executioner.service.TaskQueryService;
import pragmasoft.andriilupynos.js_executioner.service.TaskScheduleService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class TaskController {

    @Autowired private TaskScheduleService taskScheduleService;
    @Autowired private TaskExecuteService taskExecuteService;
    @Autowired private TaskQueryService taskQueryService;
    @Autowired private TaskStore taskStore;

    /**
     * Create a task for execution
     */
    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<TaskCreateRespDto> createTask(@RequestBody TaskCreateRqDto rq) {
        return taskScheduleService.schedule(
                TaskScheduleService.TaskScheduleModel.builder()
                        .code(rq.getCode())
                        .name(rq.getName())
                        .executionDate(rq.getExecutionDate())
                        .build()
        )
                .map(task ->
                    TaskCreateRespDto.builder()
                            .task(toTaskDto(task))
                            .build()
                )
                .onErrorResume(err -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, err.getMessage())));
    }

    /**
     * Find task by id, including current execution output
     */
    @GetMapping("/tasks/{id}")
    public Mono<TaskDto> findTaskById(@PathVariable String id) {
        return taskQueryService.getTaskWithCurrentOutputById(id)
                .map(this::toTaskDto)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    /**
     * Return all tasks without theirs current output.
     * Optionally filter by status or sort be createdDate
     */
    @GetMapping("/tasks")
    public Flux<TaskDto> findTasks(
            @RequestParam(required = false) TaskStatusDto status,
            @RequestParam(required = false) Boolean newFirst
    ) {
        return taskQueryService.getTasksMatching(status, newFirst)
                .map(this::toTaskDto);
    }

    /**
     * Find task by name
     */
    @GetMapping("/tasks/actions/find-by-name")
    public Mono<TaskDto> findTaskByName(@RequestParam String name) {
        return taskStore.findByName(name)
                .map(this::toTaskDto)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    /**
     * Stop task's execution
     */
    @PutMapping("/tasks/{id}/stop-execution")
    public Mono<Void> stopTask(@PathVariable String id) {
        return taskExecuteService.stopById(id);
    }

    private TaskDto toTaskDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .name(task.getName())
                .code(task.getCode())
                .output(task.getOutput())
                .error(task.getError())
                .status(TaskStatusDto.valueOf(task.getStatus().name()))
                .scheduledAt(task.getScheduledAt())
                .beginExecDate(task.getBeginExecDate())
                .endExecDate(task.getEndExecDate())
                .createdDate(task.getCreatedDate())
                .build();
    }

}
