package rpc.message;

//TODO:remove it
public class AbstractMessage<T> {
    private int type;
    private T body;

    public AbstractMessage(int type, T body) {
        this.type = type;
        this.body = body;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
