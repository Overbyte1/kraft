package common.message.command;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class SetCommand extends ModifiedCommand implements Serializable {
    private String key;
    private byte[] value;

    public SetCommand(String key, byte[] value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SetCommand{");
        sb.append("key='").append(key).append('\'');
        sb.append(", value=");
        if (value == null) sb.append("null");
        else {
            sb.append('[');
            for (int i = 0; i < value.length; ++i)
                sb.append(i == 0 ? "" : ", ").append(value[i]);
            sb.append(']');
        }
        sb.append(", requestId='").append(requestId).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SetCommand that = (SetCommand) o;
        return Objects.equals(key, that.key) &&
                Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), key);
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }
}
