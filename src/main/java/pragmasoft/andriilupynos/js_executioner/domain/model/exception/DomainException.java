package pragmasoft.andriilupynos.js_executioner.domain.model.exception;

public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

}
