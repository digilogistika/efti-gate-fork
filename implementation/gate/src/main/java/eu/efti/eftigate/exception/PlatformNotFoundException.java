package eu.efti.eftigate.exception;

public class PlatformNotFoundException extends RuntimeException {
    public PlatformNotFoundException(String message) {
        super(message);
    }
}
