package bg.sofia.uni.fmi.mjt.splitwise.exceptions;

public class FriendshipAlreadyExistsException extends SplitWiseException {
    public FriendshipAlreadyExistsException(String message) {
        super(message);
    }

    public FriendshipAlreadyExistsException(String message,Throwable cause) {
        super(message, cause);
    }
}