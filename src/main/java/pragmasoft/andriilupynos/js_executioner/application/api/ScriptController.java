package pragmasoft.andriilupynos.js_executioner.application.api;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pragmasoft.andriilupynos.js_executioner.application.api.dto.*;
import pragmasoft.andriilupynos.js_executioner.domain.ScriptInfo;
import pragmasoft.andriilupynos.js_executioner.domain.ScriptService;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(produces = "application/hal+json")
public class ScriptController {

    public static final String HATEOAS_SCRIPTS_REL = "scripts";
    public static final String HATEOAS_SCRIPT_REL = "script";

    private final ScriptService scriptService;

    public ScriptController(ScriptService scriptService) {
        this.scriptService = scriptService;
    }

    @Operation(
            operationId = "scheduleScript",
            summary = "Schedule a script for future execution.",
            description = "Schedule a script for future execution. " +
                    "It will be executed at the specified time or as soon as possible."
    )
    @PostMapping(path = "/scripts")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public EntityModel<ScriptCreateRespDto> scheduleScript(@RequestBody ScriptCreateRqDto rq) {
        var script = scriptService.create(rq.getCode(), rq.getName());
        scriptService.execute(script);
        //noinspection ConstantConditions
        return EntityModel.of(
                new ScriptCreateRespDto(script.name),
                linkTo(methodOn(ScriptController.class).scheduleScript(null)).withSelfRel(),
                linkTo(methodOn(ScriptController.class).findScriptFullInfoById(script.name)).withRel(HATEOAS_SCRIPT_REL),
                linkTo(methodOn(ScriptController.class).findScriptsSimpleInfo(null, null)).withRel(HATEOAS_SCRIPTS_REL)
        );
    }

    @Operation(
            operationId = "findScriptFullInfoByName",
            summary = "Find a script by name."
    )
    @GetMapping("/scripts/{name}")
    public EntityModel<ScriptDto> findScriptFullInfoById(@PathVariable String name) {
        var scriptInfo = scriptService.get(name);
        var scriptExecution = scriptService.executionOf(scriptInfo);
        return EntityModel.of(
                new ScriptDto(scriptInfo, scriptExecution.orElse(null)),
                linkTo(methodOn(ScriptController.class).findScriptFullInfoById(name)).withSelfRel(),
                linkTo(methodOn(ScriptController.class).findScriptsSimpleInfo(null, null)).withRel(HATEOAS_SCRIPTS_REL)
        );
    }

    @Operation(
            operationId = "deleteScriptById",
            summary = "Delete a script by id.",
            description = "Deletes a script, stopping current execution if it is being performed."
    )
    @DeleteMapping("/scripts/{name}")
    // Here we do not care about return of generic wildcard type from method
    @SuppressWarnings("java:S1452")
    public RepresentationModel<?> deleteScriptById(@PathVariable String name) {
        scriptService.delete(name);
        return new RepresentationModel<>(
                linkTo(methodOn(ScriptController.class).findScriptsSimpleInfo(null, null)).withRel(HATEOAS_SCRIPTS_REL)
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
        var scripts = scriptService.all(
                        Boolean.TRUE.equals(newFirst) ? ScriptService.SortBy.CREATED : null,
                        status != null ? ScriptInfo.Status.valueOf(status.name()) : null
                )
                .stream()
                .map(ScriptSimpleDto::new)
                .map(scriptDto ->
                        EntityModel.of(
                                scriptDto,
                                linkTo(methodOn(ScriptController.class).findScriptFullInfoById(scriptDto.getName())).withSelfRel()
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
    @DeleteMapping("/scripts/{name}/execution")
    @ResponseStatus(HttpStatus.ACCEPTED)
    // Here we do not care about return of generic wildcard type from method
    @SuppressWarnings("java:S1452")
    public RepresentationModel<?> changeScriptExecution(
            @PathVariable String name
    ) {
        scriptService.stopExecution(name);
        return RepresentationModel.of(
                null,
                List.of(
                        linkTo(methodOn(ScriptController.class).changeScriptExecution(name)).withSelfRel(),
                        linkTo(methodOn(ScriptController.class).findScriptFullInfoById(name)).withRel(HATEOAS_SCRIPT_REL)
                )
        );
    }

    @Operation(
            operationId = "getScriptExecution",
            summary = "Get information about script execution. "
    )
    @GetMapping("/scripts/{name}/execution")
    public EntityModel<ScriptExecutionDto> getScriptExecution(@PathVariable String name) {
        var scriptInfo = scriptService.get(name);
        var scriptExecution = scriptService.executionOf(scriptInfo);
        return EntityModel.of(
                new ScriptExecutionDto(scriptInfo, scriptExecution.orElse(null)),
                linkTo(methodOn(ScriptController.class).getScriptExecution(name)).withSelfRel(),
                linkTo(methodOn(ScriptController.class).findScriptFullInfoById(name)).withRel(HATEOAS_SCRIPT_REL)
        );
    }

}
