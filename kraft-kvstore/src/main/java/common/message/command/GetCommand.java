package common.message.command;


import java.io.Serializable;
import java.util.Objects;

public class GetCommand implements Serializable {
    private String key;

    public GetCommand(String key) {
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
        final StringBuffer sb = new StringBuffer("GetCommand{");
        sb.append("key='").append(key).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GetCommand that = (GetCommand) o;

        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }
}
