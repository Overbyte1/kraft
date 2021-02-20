package rpc.exception;

public class NetworkException extends RuntimeException {
    public NetworkException() {
        super();
    }

    public NetworkException(String errorMsg) {
        super(errorMsg);
    }
}
