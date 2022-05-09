package pragmasoft.andriilupynos.js_executioner.domain.model.exception;

import pragmasoft.andriilupynos.js_executioner.domain.model.script.ExecutionStatus;

public class InvalidExecutionStateException extends DomainException {

    public InvalidExecutionStateException(
            String scriptId,
            String scriptAction,
            ExecutionStatus currentStatus,
            ExecutionStatus requiredStatus
    ) {
        super("Could not perform action \"" + scriptAction + "\" on script with id " + scriptId + ": " +
                "current status is " + currentStatus + ", but required status is " + requiredStatus);
    }

}
