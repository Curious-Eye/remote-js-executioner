package pragmasoft.andriilupynos.js_executioner.api;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pragmasoft.andriilupynos.js_executioner.api.dto.*;
import pragmasoft.andriilupynos.js_executioner.data.domain.Script;
import pragmasoft.andriilupynos.js_executioner.data.domain.ScriptStatus;
import pragmasoft.andriilupynos.js_executioner.service.ScriptDeleteService;
import pragmasoft.andriilupynos.js_executioner.service.ScriptExecuteService;
import pragmasoft.andriilupynos.js_executioner.service.ScriptQueryService;
import pragmasoft.andriilupynos.js_executioner.service.ScriptScheduleService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping
public class ScriptController {

    @Autowired private ScriptScheduleService scriptScheduleService;
    @Autowired private ScriptExecuteService scriptExecuteService;
    @Autowired private ScriptQueryService scriptQueryService;
    @Autowired private ScriptDeleteService scriptDeleteService;

    @Operation(
            operationId = "createScript",
            summary = "Create a script for future execution.",
            description = "Create a script for future execution. " +
                    "It will be executed at the specified time or as soon as possible."
    )
    @PostMapping("/scripts")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<EntityModel<ScriptCreateRespDto>> createScript(@RequestBody ScriptCreateRqDto rq) {
        return scriptScheduleService.schedule(
                        ScriptScheduleService.ScriptScheduleModel.builder()
                                .code(rq.getCode())
                                .name(rq.getName())
                                .executionDate(rq.getExecutionDate())
                                .build()
                )
                .map(script -> {
                    var scriptDto = toScriptDto(script);
                    return EntityModel.of(
                            ScriptCreateRespDto.builder()
                                    .script(scriptDto)
                                    .build(),
                            getScriptHateoasLinks(scriptDto)
                    );
                });
    }

    @Operation(
            operationId = "findScriptById",
            summary = "Find a script by id."
    )
    @GetMapping("/scripts/{id}")
    public Mono<EntityModel<ScriptDto>> findScriptById(@PathVariable String id) {
        return scriptQueryService.getScriptWithCurrentOutputById(id)
                .map(this::toScriptDto)
                .map(scriptDto -> EntityModel.of(scriptDto, getScriptHateoasLinks(scriptDto)));
    }

    @Operation(
            operationId = "deleteScriptById",
            summary = "Delete a script by id.",
            description = "Deletes a script, stopping current execution if it is being performed."
    )
    @DeleteMapping("/scripts/{id}")
    public Mono<RepresentationModel<?>> deleteScriptById(@PathVariable String id) {
        return scriptDeleteService.deleteById(id)
                .thenReturn(
                        new RepresentationModel<>(
                                linkTo(methodOn(ScriptController.class).findScripts(null, null, null))
                                        .withRel("scripts")
                        )
                );
    }

    @Operation(
            operationId = "findScripts",
            summary = "Returns scripts matching provided parameters.",
            description = "Returns scripts matching provided parameters. " +
                    "Allows to filter returned scripts by status, name and to order by creation date."
    )
    @GetMapping("/scripts")
    public Flux<EntityModel<ScriptDto>> findScripts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ScriptStatusDto status,
            @RequestParam(required = false) Boolean newFirst
    ) {
        var model =
                ScriptQueryService.ScriptSearchModel.builder()
                        .newFirst(newFirst)
                        .name(name);
        if (status != null)
            model.status(ScriptStatus.valueOf(status.name()));
        return scriptQueryService.search(model.build())
                .map(this::toScriptDto)
                .map(scriptDto -> EntityModel.of(scriptDto, getScriptHateoasLinks(scriptDto)));
    }

    @Operation(
            operationId = "changeScriptExecution",
            summary = "Change execution of a script. ",
            description = "Change execution of a script. " +
                    "Currently allows only to stop the script. " +
                    "Does nothing if script with such id does not exist or is not being executed."
    )
    @PutMapping("/scripts/{id}/execution")
    public Mono<RepresentationModel<?>> changeScriptExecution(
            @PathVariable String id,
            @RequestBody ScriptChangeExecutionRqDto rq
    ) {
        return scriptExecuteService.changeExecution(
                        id,
                        ScriptExecuteService.ChangeExecutionModel.builder()
                                .action(ScriptExecuteService.ChangeExecutionAction.valueOf(rq.getAction().name()))
                                .build()
                )
                .thenReturn(RepresentationModel.of(
                        null,
                        List.of(
                                linkTo(methodOn(ScriptController.class).findScripts(null, null, null))
                                        .withRel("scripts"),
                                linkTo(methodOn(ScriptController.class).findScriptById(id))
                                        .withRel("script")
                        )
                ));
    }

    private ScriptDto toScriptDto(Script script) {
        return ScriptDto.builder()
                .id(script.getId())
                .name(script.getName())
                .code(script.getCode())
                .output(script.getOutput())
                .error(script.getError())
                .status(ScriptStatusDto.valueOf(script.getStatus().name()))
                .scheduledAt(script.getScheduledAt())
                .beginExecDate(script.getBeginExecDate())
                .endExecDate(script.getEndExecDate())
                .createdDate(script.getCreatedDate())
                .build();
    }

    private List<Link> getScriptHateoasLinks(ScriptDto scriptDto) {
        var links = new ArrayList<Link>();
        links.add(linkTo(methodOn(ScriptController.class).findScriptById(scriptDto.getId())).withSelfRel());
        links.add(linkTo(methodOn(ScriptController.class).findScripts(null, null, null)).withRel("scripts"));
        if (scriptDto.getStatus() == ScriptStatusDto.EXECUTING) {
            links.add(
                    linkTo(
                            methodOn(ScriptController.class)
                                    .changeScriptExecution(
                                            scriptDto.getId(),
                                            ScriptChangeExecutionRqDto.builder()
                                                    .action(ScriptChangeExecutionRqDto.ChangeExecutionAction.STOP)
                                                    .build()
                                    )
                    )
                            .withRel("script-execution")
            );
        }
        return links;
    }

}
