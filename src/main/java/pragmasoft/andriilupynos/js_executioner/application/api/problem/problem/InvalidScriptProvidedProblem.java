package pragmasoft.andriilupynos.js_executioner.application.api.problem.problem;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

@ResponseStatus(HttpStatus.BAD_REQUEST)
// Here we do not care about too deep of an inheritance
@SuppressWarnings("java:S110")
public class InvalidScriptProvidedProblem extends AbstractThrowableProblem {
    public InvalidScriptProvidedProblem(String baseProblemUri, String detail) {
        super(
                URI.create(baseProblemUri + "/invalid-script-provided"),
                Status.BAD_REQUEST.getReasonPhrase(),
                Status.BAD_REQUEST,
                detail
        );
    }
}
