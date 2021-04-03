package common.message.command;

import java.io.Serializable;
import java.util.Objects;

public class DelCommand extends ModifiedCommand implements Serializable {
    private String key;

    public DelCommand(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DelCommand{");
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
        DelCommand that = (DelCommand) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), key);
    }
}
