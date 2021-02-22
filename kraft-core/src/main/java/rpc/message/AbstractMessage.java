package rpc.message;

import election.node.NodeId;

import java.io.Serializable;

//TODO:remove it
public class AbstractMessage<T> implements Serializable {
    private int type;
    private NodeId nodeId;
    private T body;

    public NodeId getNodeId() {
        return nodeId;
    }

    public void setNodeId(NodeId nodeId) {
        this.nodeId = nodeId;
    }

    public AbstractMessage(int type, NodeId nodeId, T body) {
        this.type = type;
        this.body = body;
        this.nodeId = nodeId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "AbstractMessage{" +
                "type=" + type +
                ", nodeId=" + nodeId +
                ", body=" + body +
                '}';
    }
}
