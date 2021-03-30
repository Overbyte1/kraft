package common.message;

public class SetCommand extends KVCommand {
    private byte[] value;
    public SetCommand(String requestId, String key, byte[] value) {
        super(requestId, OperationType.KV_SET, key);
        this.value = value;
    }
}
