package pragmasoft.andriilupynos.js_executioner.application.api.problem.problem;

import org.zalando.problem.ThrowableProblem;
import pragmasoft.andriilupynos.js_executioner.domain.model.exception.DomainException;

public interface DomainExceptionConverter {

    ThrowableProblem toProblem(DomainException e);
    boolean supports(Class<?> exClass);

}
