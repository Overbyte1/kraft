package election.node;

import election.role.RoleType;
import election.statemachine.StateMachine;
import rpc.NodeEndpoint;

public interface Node {
    void start();
    void stop();
    boolean appendLog(byte[] command);
    boolean isLeader();
    RoleType getRoleType();
    long getCurrentTerm();
    NodeEndpoint getLeaderNodeEndpoint();
    NodeEndpoint[] getAllNodeEndpoint();
    void registerStateMachine(StateMachine stateMachine);
}
