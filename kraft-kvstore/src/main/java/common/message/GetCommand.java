package common.message;

public class GetCommand extends KVCommand {
    public GetCommand(String requestId, String key) {
        super(requestId, OperationType.KV_GET, key);
    }
}
