package bg.sofia.uni.fmi.mjt.splitwise.exceptions;

public class SplitWiseException extends Exception {

    public SplitWiseException(String message) {
        super(message);
    }

    public SplitWiseException(String message, Throwable cause) {
        super(message, cause);
    }
}