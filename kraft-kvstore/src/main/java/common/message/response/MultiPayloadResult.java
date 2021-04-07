package common.message.response;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class MultiPayloadResult implements Serializable {
    private int code;
    private byte[][] payload;

    public MultiPayloadResult(int code, byte[][] payload) {
        this.code = code;
        this.payload = payload;
    }

    public int getCode() {
        return code;
    }

    public byte[][] getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MultiPayloadResult{");
        sb.append("code=").append(code);
        sb.append(", payload=").append(payload == null ? "null" : Arrays.asList(payload).toString());
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiPayloadResult that = (MultiPayloadResult) o;
        boolean equal = code == that.code;
        for(int i = 0; i < payload.length; i++) {
            if(!Arrays.equals(payload[i], that.getPayload()[i])) {
                equal = false;
                break;
            }
        }
        return equal;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(code);
        result = 31 * result + Arrays.hashCode(payload);
        return result;
    }
}
