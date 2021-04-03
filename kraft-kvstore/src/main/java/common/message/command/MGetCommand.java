package common.message.command;

import java.io.Serializable;
import java.util.Arrays;

public class MGetCommand implements Serializable {
    private String[] keys;

    public MGetCommand(String[] keys) {
        this.keys = keys;
    }

    public String[] getKeys() {
        return keys;
    }

    public void setKeys(String[] keys) {
        this.keys = keys;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MGetCommand{");
        sb.append("keys=").append(keys == null ? "null" : Arrays.asList(keys).toString());
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MGetCommand that = (MGetCommand) o;
        return Arrays.equals(keys, that.keys);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(keys);
    }
}
