package bg.sofia.uni.fmi.mjt.splitwise.exceptions;

public class InvalidCommandArgumentException extends SplitWiseException {
    public InvalidCommandArgumentException(String message) {
        super(message);
    }

    public InvalidCommandArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}