package pragmasoft.andriilupynos.js_executioner.api;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pragmasoft.andriilupynos.js_executioner.api.dto.*;
import pragmasoft.andriilupynos.js_executioner.data.domain.Task;
import pragmasoft.andriilupynos.js_executioner.data.domain.TaskStatus;
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

    @Operation(
            operationId = "createTask",
            summary = "Create a task for future execution.",
            description = "Create a task for future execution. " +
                    "It will be executed at the specified time or as soon as possible."
    )
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

    @Operation(
            operationId = "findTaskById",
            summary = "Find a task by id."
    )
    @GetMapping("/tasks/{id}")
    public Mono<EntityModel<TaskDto>> findTaskById(@PathVariable String id) {
        return taskQueryService.getTaskWithCurrentOutputById(id)
                .map(this::toTaskDto)
                .map(taskDto -> EntityModel.of(taskDto, getTaskHateoasLinks(taskDto)));
    }

    @Operation(
            operationId = "findTasks",
            summary = "Returns tasks matching provided parameters.",
            description = "Returns tasks matching provided parameters. " +
                    "Allows to filter returned tasks by status, name and to order by creation date."
    )
    @GetMapping("/tasks")
    public Flux<EntityModel<TaskDto>> findTasks(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) TaskStatusDto status,
            @RequestParam(required = false) Boolean newFirst
    ) {
        var model =
                TaskQueryService.TaskSearchModel.builder()
                        .newFirst(newFirst)
                        .name(name);
        if (status != null)
            model.status(TaskStatus.valueOf(status.name()));
        return taskQueryService.search(model.build())
                .map(this::toTaskDto)
                .map(taskDto -> EntityModel.of(taskDto, getTaskHateoasLinks(taskDto)));
    }

    @Operation(
            operationId = "changeTaskExecution",
            summary = "Change execution of a task. ",
            description = "Change execution of a task. " +
                    "Currently allows only to stop the task. " +
                    "Does nothing if task with such id does not exist or is not being executed."
    )
    @PutMapping("/tasks/{id}/execution")
    public Mono<RepresentationModel<?>> changeTaskExecution(
            @PathVariable String id,
            @RequestBody TaskChangeExecutionRqDto rq
    ) {
        return taskExecuteService.changeExecution(
                        id,
                        TaskExecuteService.ChangeExecutionModel.builder()
                                .action(TaskExecuteService.ChangeExecutionAction.valueOf(rq.getAction().name()))
                                .build()
                )
                .thenReturn(RepresentationModel.of(
                        null,
                        List.of(
                                linkTo(methodOn(TaskController.class).findTasks(null, null, null))
                                        .withRel("tasks"),
                                linkTo(methodOn(TaskController.class).findTaskById(id))
                                        .withRel("task")
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
        links.add(linkTo(methodOn(TaskController.class).findTasks(null, null, null)).withRel("tasks"));
        if (taskDto.getStatus() == TaskStatusDto.EXECUTING) {
            links.add(
                    linkTo(
                            methodOn(TaskController.class)
                                    .changeTaskExecution(
                                            taskDto.getId(),
                                            TaskChangeExecutionRqDto.builder()
                                                    .action(TaskChangeExecutionRqDto.ChangeExecutionAction.STOP)
                                                    .build()
                                    )
                    )
                            .withRel("task-execution")
            );
        }
        return links;
    }

}
