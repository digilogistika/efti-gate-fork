package eu.efti.eftigate.exception;

public class PlatformAlreadyExistsException extends RuntimeException {
    public PlatformAlreadyExistsException(final String message) {
        super(message);
    }
}
