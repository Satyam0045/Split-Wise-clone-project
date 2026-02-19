package bg.sofia.uni.fmi.mjt.splitwise.exceptions;

public class GroupNotFoundException extends SplitWiseException {
    public GroupNotFoundException(String message) {
        super(message);
    }

    public GroupNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}