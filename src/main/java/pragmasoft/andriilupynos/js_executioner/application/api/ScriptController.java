package pragmasoft.andriilupynos.js_executioner.application.api;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pragmasoft.andriilupynos.js_executioner.application.api.dto.*;
import pragmasoft.andriilupynos.js_executioner.domain.model.script.ExecutionStatus;
import pragmasoft.andriilupynos.js_executioner.domain.model.script.ScriptFindFilter;
import pragmasoft.andriilupynos.js_executioner.domain.service.ScriptService;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(produces = "application/hal+json")
public class ScriptController {

    @Autowired private ScriptService scriptService;

    @Operation(
            operationId = "scheduleScript",
            summary = "Schedule a script for future execution.",
            description = "Schedule a script for future execution. " +
                    "It will be executed at the specified time or as soon as possible."
    )
    @PostMapping(path = "/scripts")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public EntityModel<ScriptCreateRespDto> scheduleScript(@RequestBody ScriptCreateRqDto rq) {
        var id = scriptService.scheduleScript(rq.getCode(), rq.getExecutionDate());
        return EntityModel.of(
                new ScriptCreateRespDto(id),
                linkTo(methodOn(ScriptController.class).findScriptFullInfoById(id)).withSelfRel(),
                linkTo(methodOn(ScriptController.class).findScriptsSimpleInfo(null, null)).withRel("scripts")
        );
    }

    @Operation(
            operationId = "findScriptFullInfoById",
            summary = "Find a script by id."
    )
    @GetMapping("/scripts/{id}")
    public EntityModel<ScriptDto> findScriptFullInfoById(@PathVariable String id) {
        return EntityModel.of(
                new ScriptDto(scriptService.getFullInfoById(id)),
                linkTo(methodOn(ScriptController.class).findScriptFullInfoById(id)).withSelfRel(),
                linkTo(methodOn(ScriptController.class).findScriptsSimpleInfo(null, null)).withRel("scripts")
        );
    }

    @Operation(
            operationId = "deleteScriptById",
            summary = "Delete a script by id.",
            description = "Deletes a script, stopping current execution if it is being performed."
    )
    @DeleteMapping("/scripts/{id}")
    public RepresentationModel<?> deleteScriptById(@PathVariable String id) {
        scriptService.deleteById(id);
        return new RepresentationModel<>(
                linkTo(methodOn(ScriptController.class).findScriptsSimpleInfo(null, null)).withRel("scripts")
        );
    }

    @Operation(
            operationId = "findScriptsSimpleInfo",
            summary = "Returns scripts matching provided parameters.",
            description = "Returns scripts matching provided parameters. " +
                    "Allows to filter returned scripts by status and to order by creation date."
    )
    @GetMapping("/scripts")
    public CollectionModel<EntityModel<ScriptSimpleDto>> findScriptsSimpleInfo(
            @RequestParam(required = false) ScriptExecutionStatusDto status,
            @RequestParam(required = false) Boolean newFirst
    ) {
        var filterBuilder =
                ScriptFindFilter.builder()
                        .newFirst(newFirst);
        if (status != null)
            filterBuilder.status(ExecutionStatus.valueOf(status.name()));

        var scripts = scriptService.getShortInfoMatching(filterBuilder.build())
                .stream()
                .map(ScriptSimpleDto::new)
                .map(scriptDto ->
                        EntityModel.of(
                                scriptDto,
                                linkTo(methodOn(ScriptController.class).findScriptFullInfoById(scriptDto.getId())).withSelfRel()
                        )
                )
                .collect(Collectors.toList());

        return CollectionModel.of(
                scripts,
                linkTo(methodOn(ScriptController.class).findScriptsSimpleInfo(null, null)).withSelfRel()
        );
    }

    @Operation(
            operationId = "changeScriptExecution",
            summary = "Change execution of a script. ",
            description = "Change execution of a script. " +
                    "Currently allows only to stop the script. " +
                    "Does nothing if script is not being executed."
    )
    @PatchMapping("/scripts/{id}/execution")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RepresentationModel<?> changeScriptExecution(
            @PathVariable String id,
            @RequestBody ScriptChangeExecutionRqDto rq
    ) {
        scriptService.changeExecution(id, ExecutionStatus.valueOf(rq.getStatus().name()));
        return RepresentationModel.of(
                null,
                List.of(
                        linkTo(methodOn(ScriptController.class).getScriptExecution(id)).withSelfRel(),
                        linkTo(methodOn(ScriptController.class).findScriptFullInfoById(id)).withRel("script")
                )
        );
    }

    @Operation(
            operationId = "getScriptExecution",
            summary = "Get information about script execution. "
    )
    @GetMapping("/scripts/{id}/execution")
    public EntityModel<ScriptExecutionDto> getScriptExecution(@PathVariable String id) {
        return EntityModel.of(
                new ScriptExecutionDto(scriptService.getExecutionInfo(id)),
                linkTo(methodOn(ScriptController.class).getScriptExecution(id)).withSelfRel(),
                linkTo(methodOn(ScriptController.class).findScriptFullInfoById(id)).withRel("script")
        );
    }

}
