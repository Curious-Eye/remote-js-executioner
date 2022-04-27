package pragmasoft.andriilupynos.js_executioner.exception;

import org.springframework.http.HttpStatus;

public class IllegalArgumentException extends BaseException {

    public IllegalArgumentException(String message) {
        super("illegal_argument_exception", HttpStatus.BAD_REQUEST, message);
    }

}
