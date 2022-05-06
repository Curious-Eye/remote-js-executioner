package pragmasoft.andriilupynos.js_executioner.application.api.problem.problem;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import pragmasoft.andriilupynos.js_executioner.domain.model.exception.DomainException;
import pragmasoft.andriilupynos.js_executioner.domain.model.exception.IllegalArgumentException;

import java.net.URI;

@Component
public class IllegalArgumentConverter implements DomainExceptionConverter {

    @Value("${problem.base-uri}")
    private String baseProblemUri;

    @Override
    public ThrowableProblem toProblem(DomainException e) {
        IllegalArgumentException ex = (IllegalArgumentException) e;
        return Problem.builder()
                .withType(URI.create(baseProblemUri + "/illegal-argument"))
                .withTitle(Status.BAD_REQUEST.getReasonPhrase())
                .withStatus(Status.BAD_REQUEST)
                .withDetail(ex.getMessage())
                .withCause(new ThrowableProblem() {})
                .build();
    }

    @Override
    public boolean supports(Class<?> exClass) {
        return IllegalArgumentException.class.isAssignableFrom(exClass);
    }

}
