package eu.efti.eftigate.exception;

public class GateAlreadyExistsException extends RuntimeException {
    public GateAlreadyExistsException(String message) {
        super(message);
    }
}
