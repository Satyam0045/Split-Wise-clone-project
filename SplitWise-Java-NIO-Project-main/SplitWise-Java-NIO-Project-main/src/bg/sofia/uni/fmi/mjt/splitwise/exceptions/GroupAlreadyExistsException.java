package bg.sofia.uni.fmi.mjt.splitwise.exceptions;

public class GroupAlreadyExistsException extends SplitWiseException {
    public GroupAlreadyExistsException(String message) {
        super(message);
    }

    public GroupAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}