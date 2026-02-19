package bg.sofia.uni.fmi.mjt.splitwise.exceptions;

public class AlreadyLoggedInException extends SplitWiseException {
    public AlreadyLoggedInException(String message) {
        super(message);
    }

    public AlreadyLoggedInException(String message, Throwable cause) {
        super(message, cause);
    }
}