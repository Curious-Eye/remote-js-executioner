package pragmasoft.andriilupynos.js_executioner.exception;

import org.springframework.http.HttpStatus;

public class TaskNotFoundException extends BaseException {

    public TaskNotFoundException(String message) {
        super("task_not_found_exception", HttpStatus.NOT_FOUND, message);
    }

}
