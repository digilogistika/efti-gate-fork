package eu.efti.eftigate.exception;

public class AuthorityUserAlreadyExistsException extends RuntimeException {
    public AuthorityUserAlreadyExistsException(String message) {
        super(message);
    }
}
