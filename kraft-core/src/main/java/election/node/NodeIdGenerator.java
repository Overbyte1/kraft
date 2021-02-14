package election.node;

import java.util.UUID;

public class NodeIdGenerator {
    public static NodeId generate() {
        String value = UUID.randomUUID().toString();
        return new NodeId(value);
    }
}
