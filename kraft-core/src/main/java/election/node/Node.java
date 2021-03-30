package election.node;

import rpc.NodeEndpoint;

public interface Node {
    void start();
    void stop();
    boolean appendLog(byte[] command);
    boolean isLeader();
    NodeEndpoint getLeaderNodeEndpoint();
}
