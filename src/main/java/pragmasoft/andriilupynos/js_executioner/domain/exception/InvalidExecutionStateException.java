package pragmasoft.andriilupynos.js_executioner.domain.exception;

import pragmasoft.andriilupynos.js_executioner.domain.ScriptException;

import java.util.Collection;

public class InvalidExecutionStateException extends ScriptException {

    public InvalidExecutionStateException(
            String scriptName,
            String desiredStatus,
            String currentStatus,
            Collection<String> requiredStatus
    ) {
        super("Could not change status to \"" + desiredStatus + "\"" +
                " for execution of script with name " + scriptName + ": " +
                "current status is " + currentStatus + ", but required status one of " + requiredStatus);
    }

}
