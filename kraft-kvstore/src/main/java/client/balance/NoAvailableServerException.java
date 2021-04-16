package client.balance;

public class NoAvailableServerException extends RuntimeException {
    public NoAvailableServerException(String errMsg) {
        super(errMsg);
    }
}
