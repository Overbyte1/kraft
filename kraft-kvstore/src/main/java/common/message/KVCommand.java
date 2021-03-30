package common.message;

public abstract class KVCommand {
    private String requestId;
    private int operationType;
    private String key;

    public KVCommand(String requestId, int operationType, String key) {
        this.requestId = requestId;
        this.operationType = operationType;
        this.key = key;
    }
}
