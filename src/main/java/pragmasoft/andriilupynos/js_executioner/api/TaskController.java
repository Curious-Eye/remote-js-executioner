package pragmasoft.andriilupynos.js_executioner.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
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

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping
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
    public Mono<EntityModel<TaskCreateRespDto>> createTask(@RequestBody TaskCreateRqDto rq) {
        return taskScheduleService.schedule(
                        TaskScheduleService.TaskScheduleModel.builder()
                                .code(rq.getCode())
                                .name(rq.getName())
                                .executionDate(rq.getExecutionDate())
                                .build()
                )
                .map(task -> {
                    var taskDto = toTaskDto(task);
                    return EntityModel.of(
                            TaskCreateRespDto.builder()
                                    .task(taskDto)
                                    .build(),
                            getTaskHateoasLinks(taskDto)
                    );
                })
                .onErrorResume(err -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, err.getMessage())));
    }

    /**
     * Find task by id, including current execution output
     */
    @GetMapping("/tasks/{id}")
    public Mono<EntityModel<TaskDto>> findTaskById(@PathVariable String id) {
        return taskQueryService.getTaskWithCurrentOutputById(id)
                .map(this::toTaskDto)
                .map(taskDto -> EntityModel.of(taskDto, getTaskHateoasLinks(taskDto)))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    /**
     * Return all tasks without theirs current output.
     * Optionally filter by status or sort be createdDate
     */
    @GetMapping("/tasks")
    public Flux<EntityModel<TaskDto>> findTasks(
            @RequestParam(required = false) TaskStatusDto status,
            @RequestParam(required = false) Boolean newFirst
    ) {
        return taskQueryService.getTasksMatching(status, newFirst)
                .map(this::toTaskDto)
                .map(taskDto -> EntityModel.of(taskDto, getTaskHateoasLinks(taskDto)));
    }

    /**
     * Find task by name
     */
    @GetMapping("/tasks/actions/find-by-name")
    public Mono<EntityModel<TaskDto>> findTaskByName(@RequestParam String name) {
        return taskStore.findByName(name)
                .map(this::toTaskDto)
                .map(taskDto -> EntityModel.of(taskDto, getTaskHateoasLinks(taskDto)))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    /**
     * Stop task's execution
     */
    @PutMapping("/tasks/{id}/stop-execution")
    public Mono<RepresentationModel<?>> stopTask(@PathVariable String id) {
        return taskExecuteService.stopById(id)
                .thenReturn(RepresentationModel.of(
                        null,
                        List.of(
                                linkTo(methodOn(TaskController.class).findTasks(null, null))
                                        .withRel("tasks")
                        )
                ));
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

    private List<Link> getTaskHateoasLinks(TaskDto taskDto) {
        var links = new ArrayList<Link>();
        links.add(linkTo(methodOn(TaskController.class).findTaskById(taskDto.getId())).withSelfRel());
        links.add(linkTo(methodOn(TaskController.class).findTasks(null, null)).withRel("tasks"));
        if (taskDto.getStatus() == TaskStatusDto.EXECUTING)
            links.add(linkTo(methodOn(TaskController.class).stopTask(taskDto.getId())).withRel("stop-task"));
        return links;
    }

}
