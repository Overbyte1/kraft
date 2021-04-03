package common.message.command;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.Arrays;

public class MSetCommand extends ModifiedCommand implements Serializable {
    private String[] keys;
    private byte[][] values;

    public MSetCommand(String[] keys, byte[][] values) {
        this.keys = keys;
        this.values = values;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MSetCommand{");
        sb.append("keys=").append(keys == null ? "null" : Arrays.asList(keys).toString());
        sb.append(", values=").append(values == null ? "null" : Arrays.asList(values).toString());
        sb.append(", requestId='").append(requestId).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MSetCommand that = (MSetCommand) o;
        return Arrays.equals(keys, that.keys) &&
                Arrays.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(keys);
        result = 31 * result + Arrays.hashCode(values);
        return result;
    }
}
