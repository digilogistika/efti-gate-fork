package eu.efti.eftigate.exception;

public class DtoValidationException extends RuntimeException {
    public DtoValidationException(String message) {
        super(message);
    }
}
