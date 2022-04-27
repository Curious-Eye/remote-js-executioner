package pragmasoft.andriilupynos.js_executioner.api.dto;

import lombok.Getter;
import org.springframework.web.server.ServerWebInputException;
import pragmasoft.andriilupynos.js_executioner.exception.BaseException;

@Getter
public class ExceptionDto {

    private String error;
    private String message;

    private ExceptionDto() {}

    public static ExceptionDto fromBusinessEx(BaseException exception) {
        var exDto = new ExceptionDto();
        exDto.error = exception.getError();
        exDto.message = exception.getMessage();
        return exDto;
    }

    public static ExceptionDto fromUnexpectedEx(Throwable ex) {
        var exDto = new ExceptionDto();
        exDto.error = chooseErrorForUnexpectedEx(ex);
        exDto.message = ex.getMessage();
        return exDto;
    }

    private static String chooseErrorForUnexpectedEx(Throwable ex) {
        if (ex instanceof ServerWebInputException)
            return "bad_request_exception";
        return "unexpected_error";
    }

}
