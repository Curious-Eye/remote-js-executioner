package pragmasoft.andriilupynos.js_executioner.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseException extends RuntimeException {

    private final HttpStatus status;
    private final String error;

    public BaseException(String error, HttpStatus status, String message) {
        super(message);
        this.error = error;
        this.status = status;
    }

}
