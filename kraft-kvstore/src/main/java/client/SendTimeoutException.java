package client;

public class SendTimeoutException extends RuntimeException{
    public SendTimeoutException(String errMsg) {
        super(errMsg);
    }
}
