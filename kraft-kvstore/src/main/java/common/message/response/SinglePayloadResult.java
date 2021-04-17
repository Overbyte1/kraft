package common.message.response;

import java.io.Serializable;
import java.util.Arrays;

public class SinglePayloadResult implements Serializable {
    private int code;
    private byte[] payload;

    public SinglePayloadResult(int code, byte[] payload) {
        this.code = code;
        this.payload = payload;
    }
    public SinglePayloadResult(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public byte[] getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SinglePayloadResult{");
        sb.append("code=").append(code);
        sb.append(", payload=");
        if (payload == null) sb.append("null");
        else {
            sb.append('[');
            for (int i = 0; i < payload.length; ++i)
                sb.append(i == 0 ? "" : ", ").append(payload[i]);
            sb.append(']');
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SinglePayloadResult that = (SinglePayloadResult) o;

        if (code != that.code) return false;
        return Arrays.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        int result = code;
        result = 31 * result + Arrays.hashCode(payload);
        return result;
    }
}
