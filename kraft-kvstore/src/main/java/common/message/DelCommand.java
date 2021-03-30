package common.message;

public class DelCommand extends KVCommand{
    public DelCommand(String requestId, String key) {
        super(requestId, OperationType.KV_DEL, key);
    }
}
