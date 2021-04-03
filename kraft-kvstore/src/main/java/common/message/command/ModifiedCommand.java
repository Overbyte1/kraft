package common.message.command;

import java.io.Serializable;
import java.util.UUID;

public abstract class ModifiedCommand implements Serializable {
    protected String requestId;

    public ModifiedCommand() {
        requestId = UUID.randomUUID().toString();
    }

    public String getRequestId() {
        return requestId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModifiedCommand that = (ModifiedCommand) o;

        return requestId != null ? requestId.equals(that.requestId) : that.requestId == null;
    }

    @Override
    public int hashCode() {
        return requestId != null ? requestId.hashCode() : 0;
    }
}
