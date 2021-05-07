package client.balance;

public class NoLeaderException extends RuntimeException {
    public NoLeaderException(String msg) {
        super(msg);
    }
}
