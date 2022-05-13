package pragmasoft.andriilupynos.js_executioner.application.api.problem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import pragmasoft.andriilupynos.js_executioner.application.api.problem.problem.IllegalArgumentProblem;
import pragmasoft.andriilupynos.js_executioner.application.api.problem.problem.InvalidExecutionStateProblem;
import pragmasoft.andriilupynos.js_executioner.application.api.problem.problem.InvalidScriptProvidedProblem;
import pragmasoft.andriilupynos.js_executioner.application.api.problem.problem.ScriptNotFoundProblem;
import pragmasoft.andriilupynos.js_executioner.domain.ScriptException;
import pragmasoft.andriilupynos.js_executioner.domain.exception.IllegalArgumentException;
import pragmasoft.andriilupynos.js_executioner.domain.exception.InvalidExecutionStateException;
import pragmasoft.andriilupynos.js_executioner.domain.exception.InvalidScriptProvidedException;
import pragmasoft.andriilupynos.js_executioner.domain.exception.ScriptNotFoundException;

import javax.annotation.Nonnull;

@RestControllerAdvice
public class GlobalExceptionsAdvice implements ProblemHandling {

    private final Logger log = LoggerFactory.getLogger(GlobalExceptionsAdvice.class);

    @Value("${problem.base-uri}")
    private String baseProblemUri;

    @Override
    public ResponseEntity<Problem> handleThrowable(@Nonnull Throwable ex, @Nonnull NativeWebRequest request) {
        this.logException(ex);
        return ProblemHandling.super.handleThrowable(ex, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Problem> convertIllegalArgumentExToProblem(IllegalArgumentException ex, NativeWebRequest request) {
        this.logException(ex);
        return ProblemHandling.super.handleThrowable(
                new IllegalArgumentProblem(baseProblemUri, ex.getMessage()),
                request
        );
    }

    @ExceptionHandler(InvalidExecutionStateException.class)
    public ResponseEntity<Problem> convertInvalidExecutionStateExToProblem(InvalidExecutionStateException ex, NativeWebRequest request) {
        this.logException(ex);
        return ProblemHandling.super.handleThrowable(
                new InvalidExecutionStateProblem(baseProblemUri, ex.getMessage()),
                request
        );
    }

    @ExceptionHandler(InvalidScriptProvidedException.class)
    public ResponseEntity<Problem> convertInvalidScriptProvidedExToProblem(InvalidScriptProvidedException ex, NativeWebRequest request) {
        this.logException(ex);
        return ProblemHandling.super.handleThrowable(
                new InvalidScriptProvidedProblem(baseProblemUri, ex.getMessage()),
                request
        );
    }

    @ExceptionHandler(ScriptNotFoundException.class)
    public ResponseEntity<Problem> convertScriptNotFoundExToProblem(ScriptNotFoundException ex, NativeWebRequest request) {
        this.logException(ex);
        return ProblemHandling.super.handleThrowable(
                new ScriptNotFoundProblem(baseProblemUri, ex.getMessage()),
                request
        );
    }

    private void logException(Throwable ex) {
        if (ScriptException.class.isAssignableFrom(ex.getClass())) {
            log.warn("BSN_EX({}): {}", ex.getClass().getSimpleName(), ex.getMessage());
        } else {
            log.error("API_ERR({}): {}", ex.getClass().getSimpleName(), ex.getMessage());
        }
    }

}
