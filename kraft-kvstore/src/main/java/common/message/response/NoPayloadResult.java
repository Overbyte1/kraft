package common.message.response;

import java.io.Serializable;
import java.util.Objects;

public class NoPayloadResult implements Serializable {
    private int code;

    public NoPayloadResult(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("NoPayloadResult{");
        sb.append("code=").append(code);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoPayloadResult that = (NoPayloadResult) o;
        return code == that.code;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
