package pragmasoft.andriilupynos.js_executioner.application.api.problem.problem;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

@ResponseStatus(HttpStatus.CONFLICT)
// Here we do not care about too deep of an inheritance
@SuppressWarnings("java:S110")
public class InvalidExecutionStateProblem extends AbstractThrowableProblem {
    public InvalidExecutionStateProblem(String baseProblemUri, String detail) {
        super(
                URI.create(baseProblemUri + "/invalid-execution-state"),
                Status.CONFLICT.getReasonPhrase(),
                Status.CONFLICT,
                detail
        );
    }
}
