package common.message.command;

import java.io.Serializable;
import java.util.Objects;

public class MDelCommand extends ModifiedCommand implements Serializable {
    private String key;

    public MDelCommand(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MDelCommand{");
        sb.append("key='").append(key).append('\'');
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
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), key);
    }
}
