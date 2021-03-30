package server;

import election.node.Node;
import rpc.NodeEndpoint;

public class NodeMock implements Node {
    @Override
    public void start() {
        System.out.println("node start");
    }

    @Override
    public void stop() {
        System.out.println("node stop");
    }

    @Override
    public boolean appendLog(byte[] command) {
        return false;
    }

    @Override
    public boolean isLeader() {
        return false;
    }

    @Override
    public NodeEndpoint getLeaderNodeEndpoint() {
        return null;
    }
}
