package common.message;

public class GeneralResult {
    private int code;
    private byte[] payload;

    public GeneralResult(int code, byte[] payload) {
        this.code = code;
        this.payload = payload;
    }
    public GeneralResult(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public byte[] getPayload() {
        return payload;
    }
}
