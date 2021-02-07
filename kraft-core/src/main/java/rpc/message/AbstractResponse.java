package rpc.message;

public abstract class AbstractResponse<T> {
    private int type;

    private T responseBody;

    public AbstractResponse(T responseBody) {
        this.responseBody = responseBody;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public T getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(T responseBody) {
        this.responseBody = responseBody;
    }
}
