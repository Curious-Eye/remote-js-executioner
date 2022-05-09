package pragmasoft.andriilupynos.js_executioner.domain.model.exception;

public class ScriptNotFoundException extends DomainException {

    public ScriptNotFoundException(String scriptId) {
        super("Script with id " + scriptId + " does not exist");
    }

}
