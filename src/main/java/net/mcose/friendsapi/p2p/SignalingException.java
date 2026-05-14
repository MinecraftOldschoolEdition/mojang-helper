package net.mcose.friendsapi.p2p;

public class SignalingException extends Exception {
    public SignalingException(String message) {
        super(message);
    }

    public SignalingException(String message, Throwable cause) {
        super(message, cause);
    }
}
