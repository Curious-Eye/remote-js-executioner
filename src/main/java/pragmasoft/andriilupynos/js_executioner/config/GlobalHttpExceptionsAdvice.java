package pragmasoft.andriilupynos.js_executioner.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.server.ServerWebInputException;
import pragmasoft.andriilupynos.js_executioner.api.dto.ExceptionDto;
import pragmasoft.andriilupynos.js_executioner.exception.BaseException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@EnableWebFlux
public class GlobalHttpExceptionsAdvice {

    private final Logger log = LoggerFactory.getLogger(GlobalHttpExceptionsAdvice.class);

    @ExceptionHandler(BaseException.class)
    public Mono<ResponseEntity<?>> handleBusinessException(BaseException ex) {
        log.warn("BSN_EX({}): {}", ex.getClass().getSimpleName(), ex.getMessage());
        return Mono.just(new ResponseEntity<>(ExceptionDto.fromBusinessEx(ex), ex.getStatus()));
    }

    @ExceptionHandler(Throwable.class)
    public Mono<ResponseEntity<?>> handleUnexpectedException(Throwable ex) {
        log.error("API_ERR({}): {}", ex.getClass().getSimpleName(), ex.getMessage());
        ex.printStackTrace();
        return Mono.just(new ResponseEntity<>(ExceptionDto.fromUnexpectedEx(ex), chooseStatusForUnexpectedEx(ex)));
    }

    private HttpStatus chooseStatusForUnexpectedEx(Throwable ex) {
        if (ex instanceof ServerWebInputException)
            return HttpStatus.BAD_REQUEST;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
