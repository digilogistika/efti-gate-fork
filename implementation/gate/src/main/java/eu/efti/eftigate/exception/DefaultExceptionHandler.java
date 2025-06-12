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
            PlatformAlreadyExistsException.class,
            AmbiguousIdentifierException.class,
            GateAlreadyExistsException.class})
    public ResponseEntity<ErrorDto> handleAlreadyExistsException(RuntimeException e) {
        log.error("Error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorDto(e.getMessage()));
    }

    @ExceptionHandler({
            RequestNotFoundException.class,
            GateDoesNotExistException.class})
    public ResponseEntity<ErrorDto> handleNotFoundException(RuntimeException e) {
        log.error("Error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto(e.getMessage()));
    }

    @ExceptionHandler({XApiKeyValidationException.class})
    public ResponseEntity<ErrorDto> handleInvalidException(RuntimeException e) {
        log.error("Error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleAllUncaughtException(Exception e) {
        log.error("Unhandled exception occurred: ", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDto("An unexpected error occurred"));
    }
}
