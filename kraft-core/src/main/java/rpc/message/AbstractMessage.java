package rpc.message;

public class AbstractMessage<T> {
    private int messageId;
    private T body;

    public AbstractMessage(int messageId, T body) {
        this.messageId = messageId;
        this.body = body;
    }
    public AbstractMessage(){}

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
