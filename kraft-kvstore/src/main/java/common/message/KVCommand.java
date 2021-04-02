package common.message;

import java.io.Serializable;

public abstract class KVCommand implements Serializable {
    private String requestId;
    private int operationType;
    private String key;

    public KVCommand(String requestId, int operationType, String key) {
        this.requestId = requestId;
        this.operationType = operationType;
        this.key = key;
    }

    public String getRequestId() {
        return requestId;
    }

    public int getOperationType() {
        return operationType;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "KVCommand{" +
                "requestId='" + requestId + '\'' +
                ", operationType=" + operationType +
                ", key='" + key + '\'' +
                '}';
    }
}
