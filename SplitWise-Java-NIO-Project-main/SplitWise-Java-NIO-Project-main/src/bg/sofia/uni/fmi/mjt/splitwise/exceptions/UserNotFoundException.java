package bg.sofia.uni.fmi.mjt.splitwise.exceptions;

public class UserNotFoundException extends SplitWiseException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}