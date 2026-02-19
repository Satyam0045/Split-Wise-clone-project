package bg.sofia.uni.fmi.mjt.splitwise.exceptions;

public class UserAlreadyExistsException extends SplitWiseException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}