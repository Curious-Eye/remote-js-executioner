package pragmasoft.andriilupynos.js_executioner.application.api.problem.problem;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.zalando.problem.AbstractThrowableProblem;
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
        return new InvalidExecutionStateProblem(baseProblemUri, ex.getMessage());
    }

    @Override
    public boolean supports(Class<?> exClass) {
        return InvalidExecutionStateException.class.isAssignableFrom(exClass);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class InvalidExecutionStateProblem extends AbstractThrowableProblem {
        public InvalidExecutionStateProblem(String baseProblemUri, String detail) {
            super(
                    URI.create(baseProblemUri + "/invalid-execution-state"),
                    Status.CONFLICT.getReasonPhrase(),
                    Status.CONFLICT,
                    detail
            );
        }
    }

}
