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
        return "GeneralResult{" +
                "code=" + code +
                ", payload=" + Arrays.toString(payload) +
                '}';
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
