package pragmasoft.andriilupynos.js_executioner.application.api.problem.problem;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import pragmasoft.andriilupynos.js_executioner.domain.model.exception.DomainException;
import pragmasoft.andriilupynos.js_executioner.domain.model.exception.InvalidJSProvidedException;

import java.net.URI;

@Component
public class InvalidJSProvidedConverter implements DomainExceptionConverter {

    @Value("${problem.base-uri}")
    private String baseProblemUri;

    @Override
    public ThrowableProblem toProblem(DomainException e) {
        InvalidJSProvidedException ex = (InvalidJSProvidedException) e;
        return new InvalidJSProvidedProblem(baseProblemUri, ex.getMessage());
    }

    @Override
    public boolean supports(Class<?> exClass) {
        return InvalidJSProvidedException.class.isAssignableFrom(exClass);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    // Here we do not care about too deep of an inheritance
    @SuppressWarnings("java:S110")
    public static class InvalidJSProvidedProblem extends AbstractThrowableProblem {
        public InvalidJSProvidedProblem(String baseProblemUri, String detail) {
            super(
                    URI.create(baseProblemUri + "/invalid-js-provided"),
                    Status.BAD_REQUEST.getReasonPhrase(),
                    Status.BAD_REQUEST,
                    detail
            );
        }
    }

}
