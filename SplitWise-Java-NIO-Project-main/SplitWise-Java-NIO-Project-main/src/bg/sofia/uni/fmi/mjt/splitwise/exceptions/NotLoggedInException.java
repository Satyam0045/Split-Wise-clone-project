package bg.sofia.uni.fmi.mjt.splitwise.exceptions;

public class NotLoggedInException extends SplitWiseException {
    public NotLoggedInException(String message) {
        super(message);
    }

    public NotLoggedInException(String message, Throwable cause) {
        super(message, cause);
    }
}