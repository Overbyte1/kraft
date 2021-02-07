package rpc.message;

public abstract class AbstractRequest<T> {
    private int type;

    public AbstractRequest(T requestBody) {
        this.requestBody = requestBody;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private T requestBody;

    public T getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(T requestBody) {
        this.requestBody = requestBody;
    }
}
