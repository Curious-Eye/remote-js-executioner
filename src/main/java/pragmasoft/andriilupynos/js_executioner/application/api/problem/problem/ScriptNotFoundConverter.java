package pragmasoft.andriilupynos.js_executioner.application.api.problem.problem;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.zalando.problem.AbstractThrowableProblem;
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
        return new ScriptNotFoundProblem(baseProblemUri, ex.getMessage());
    }

    @Override
    public boolean supports(Class<?> exClass) {
        return ScriptNotFoundException.class.isAssignableFrom(exClass);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class ScriptNotFoundProblem extends AbstractThrowableProblem {
        public ScriptNotFoundProblem(String baseProblemUri, String detail) {
            super(
                    URI.create(baseProblemUri + "/script-not-found"),
                    Status.NOT_FOUND.getReasonPhrase(),
                    Status.NOT_FOUND,
                    detail
            );
        }
    }

}
