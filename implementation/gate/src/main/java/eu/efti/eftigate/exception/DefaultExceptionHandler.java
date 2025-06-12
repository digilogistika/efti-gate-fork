package eu.efti.eftigate.exception;

import eu.efti.eftigate.dto.ErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class DefaultExceptionHandler {

    @ExceptionHandler({
            AuthorityUserAlreadyExistsException.class,
            PlatformAlreadyExistsException.class})
    public ResponseEntity<ErrorDto> handleAlreadyExistsException(RuntimeException e) {
        log.error("Error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorDto(e.getMessage()));
    }
}
