package bg.sofia.uni.fmi.mjt.splitwise.exceptions;

public class InvalidCredentialsException extends SplitWiseException {
    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}