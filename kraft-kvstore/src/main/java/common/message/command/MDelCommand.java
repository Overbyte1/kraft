package common.message.command;

import java.io.Serializable;
import java.util.Arrays;

public class MDelCommand extends ModifiedCommand implements Serializable {
    private String[] key;

    public MDelCommand(String[] key) {
        this.key = key;
    }

    public String[] getKey() {
        return key;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MDelCommand{");
        sb.append("key=").append(key == null ? "null" : Arrays.asList(key).toString());
        sb.append(", requestId='").append(requestId).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MDelCommand that = (MDelCommand) o;
        return Arrays.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(key);
        return result;
    }
}
