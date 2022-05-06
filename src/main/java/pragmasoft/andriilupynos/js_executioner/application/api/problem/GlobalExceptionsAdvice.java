package pragmasoft.andriilupynos.js_executioner.application.api.problem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import pragmasoft.andriilupynos.js_executioner.application.api.problem.problem.DomainExceptionConverter;
import pragmasoft.andriilupynos.js_executioner.domain.model.exception.DomainException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionsAdvice implements ProblemHandling {

    private final Logger log = LoggerFactory.getLogger(GlobalExceptionsAdvice.class);

    private final List<DomainExceptionConverter> exConverters;

    public GlobalExceptionsAdvice(List<DomainExceptionConverter> exConverters) {
        this.exConverters = exConverters;
    }

    @Override
    public ResponseEntity<Problem> handleThrowable(Throwable ex, NativeWebRequest request) {
        if (DomainException.class.isAssignableFrom(ex.getClass())) {
            log.warn("BSN_EX({}): {}", ex.getClass().getSimpleName(), ex.getMessage());
            var converter =
                    exConverters.stream()
                            .filter(it -> it.supports(ex.getClass()))
                            .findFirst();

            if (converter.isEmpty()) {
                log.warn("Received business exception with no to problem converter - {}: {}",
                        ex.getClass().getSimpleName(), ex.getMessage());
                return ProblemHandling.super.handleThrowable(ex, request);
            }

            var problem = converter.get().toProblem((DomainException) ex);
            return ProblemHandling.super.handleThrowable(problem, request);
        } else {
            log.error("API_ERR({}): {}", ex.getClass().getSimpleName(), ex.getMessage());
            return ProblemHandling.super.handleThrowable(ex, request);
        }
    }
}
