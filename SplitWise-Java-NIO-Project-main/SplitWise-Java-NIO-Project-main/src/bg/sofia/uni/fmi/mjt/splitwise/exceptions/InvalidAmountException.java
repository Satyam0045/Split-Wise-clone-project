package bg.sofia.uni.fmi.mjt.splitwise.exceptions;

public class InvalidAmountException extends SplitWiseException {
    public InvalidAmountException(String message) {
        super(message);
    }

    public InvalidAmountException(String message, Throwable cause) {
        super(message, cause);
    }
}