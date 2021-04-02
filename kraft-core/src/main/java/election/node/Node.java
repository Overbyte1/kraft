package election.node;

import election.statemachine.StateMachine;
import rpc.NodeEndpoint;

public interface Node {
    void start();
    void stop();
    boolean appendLog(byte[] command);
    boolean isLeader();
    NodeEndpoint getLeaderNodeEndpoint();
    void registerStateMachine(StateMachine stateMachine);
}
