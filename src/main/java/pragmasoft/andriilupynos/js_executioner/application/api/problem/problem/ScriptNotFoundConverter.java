package pragmasoft.andriilupynos.js_executioner.application.api.problem.problem;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import pragmasoft.andriilupynos.js_executioner.domain.model.exception.DomainException;
import pragmasoft.andriilupynos.js_executioner.domain.model.exception.ScriptNotFoundException;

import java.net.URI;

@Component
public class ScriptNotFoundConverter implements DomainExceptionConverter {

    @Value("${problem.base-uri}")
    private String baseProblemUri;

    @Override
    public ThrowableProblem toProblem(DomainException e) {
        ScriptNotFoundException ex = (ScriptNotFoundException) e;
        return Problem.builder()
                .withType(URI.create(baseProblemUri + "/script-not-found"))
                .withTitle(Status.NOT_FOUND.getReasonPhrase())
                .withStatus(Status.NOT_FOUND)
                .withDetail(ex.getMessage())
                .build();
    }

    @Override
    public boolean supports(Class<?> exClass) {
        return ScriptNotFoundException.class.isAssignableFrom(exClass);
    }

}
