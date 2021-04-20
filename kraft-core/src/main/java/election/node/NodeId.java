package election.node;

import java.io.Serializable;
import java.util.UUID;

public class NodeId implements Serializable, Comparable<NodeId> {
    private String value;

    public NodeId(String value) {
        this.value = value;
    }

    /**
     * 每个节点的ID都不应该相同，使用UUID作为标识
     * @return NodeId
     */
    public static NodeId createNodeId() {
        return new NodeId(UUID.randomUUID().toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeId nodeId = (NodeId) o;

        return value != null ? value.equals(nodeId.value) : nodeId.value == null;
    }


    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "NodeId{" +
                "value='" + value + '\'' +
                '}';
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int compareTo(NodeId o) {
        return value.compareTo(o.value);
    }
}
