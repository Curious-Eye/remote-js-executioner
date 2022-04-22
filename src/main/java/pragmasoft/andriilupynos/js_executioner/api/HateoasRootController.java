package pragmasoft.andriilupynos.js_executioner.api;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class HateoasRootController {

    @GetMapping
    public RepresentationModel<?> getEntitiesLinks() {
        return new RepresentationModel<>(
            List.of(
                    linkTo(methodOn(TaskController.class).findTasks(null, null))
                            .withRel("tasks")
            )
        );
    }

}
