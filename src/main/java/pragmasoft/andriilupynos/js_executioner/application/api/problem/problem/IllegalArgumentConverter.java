package pragmasoft.andriilupynos.js_executioner.application.api.problem.problem;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.zalando.problem.AbstractThrowableProblem;
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
        return new IllegalArgumentProblem(baseProblemUri, ex.getMessage());
    }

    @Override
    public boolean supports(Class<?> exClass) {
        return IllegalArgumentException.class.isAssignableFrom(exClass);
    }

    /**
     * A workaround class, because if we return Problem.builder().build()
     * from the toProblem method, then framework won't properly return
     * the problem
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class IllegalArgumentProblem extends AbstractThrowableProblem {
        public IllegalArgumentProblem(String baseProblemUri, String detail) {
            super(
                    URI.create(baseProblemUri + "/illegal-argument"),
                    Status.BAD_REQUEST.getReasonPhrase(),
                    Status.BAD_REQUEST,
                    detail
            );
        }
    }

}
