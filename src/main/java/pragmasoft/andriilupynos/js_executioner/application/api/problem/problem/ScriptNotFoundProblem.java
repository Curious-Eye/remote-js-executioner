package pragmasoft.andriilupynos.js_executioner.application.api.problem.problem;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

@ResponseStatus(HttpStatus.NOT_FOUND)
// Here we do not care about too deep of an inheritance
@SuppressWarnings("java:S110")
public class ScriptNotFoundProblem extends AbstractThrowableProblem {
    public ScriptNotFoundProblem(String baseProblemUri, String detail) {
        super(
                URI.create(baseProblemUri + "/script-not-found"),
                Status.NOT_FOUND.getReasonPhrase(),
                Status.NOT_FOUND,
                detail
        );
    }
}
