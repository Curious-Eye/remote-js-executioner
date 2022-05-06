package pragmasoft.andriilupynos.js_executioner.domain.model.exception;

public abstract class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

}
