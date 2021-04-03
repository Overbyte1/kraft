package server;

import election.node.Node;
import election.statemachine.StateMachine;
import rpc.NodeEndpoint;

public class NodeMock implements Node {
    private StateMachine stateMachine;
    private NodeEndpoint nodeEndpoint;
    private boolean leader = true;
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
        System.out.println("appendLog: " + command);
        stateMachine.apply(command);
        return true;
    }

    @Override
    public boolean isLeader() {
        return leader;
    }

    @Override
    public NodeEndpoint getLeaderNodeEndpoint() {
        return nodeEndpoint;
    }

    @Override
    public void registerStateMachine(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }

    public void setStateMachine(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }

    public NodeEndpoint getNodeEndpoint() {
        return nodeEndpoint;
    }

    public void setNodeEndpoint(NodeEndpoint nodeEndpoint) {
        this.nodeEndpoint = nodeEndpoint;
    }
}
