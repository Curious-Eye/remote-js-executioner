package pragmasoft.andriilupynos.js_executioner.application.api.problem.problem;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import pragmasoft.andriilupynos.js_executioner.domain.model.exception.DomainException;
import pragmasoft.andriilupynos.js_executioner.domain.model.exception.InvalidExecutionStateException;

import java.net.URI;

@Component
public class InvalidExecutionStateConverter implements DomainExceptionConverter {

    @Value("${problem.base-uri}")
    private String baseProblemUri;

    @Override
    public ThrowableProblem toProblem(DomainException e) {
        InvalidExecutionStateException ex = (InvalidExecutionStateException) e;
        return Problem.builder()
                .withType(URI.create(baseProblemUri + "/invalid-execution-state"))
                .withTitle(Status.CONFLICT.getReasonPhrase())
                .withStatus(Status.CONFLICT)
                .withDetail(ex.getMessage())
                .build();
    }

    @Override
    public boolean supports(Class<?> exClass) {
        return InvalidExecutionStateException.class.isAssignableFrom(exClass);
    }

}
