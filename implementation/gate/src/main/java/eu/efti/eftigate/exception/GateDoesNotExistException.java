package eu.efti.eftigate.exception;

public class GateDoesNotExistException extends RuntimeException {
    public GateDoesNotExistException(String message) {
        super(message);
    }
}
