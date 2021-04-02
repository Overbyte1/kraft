package server;

import election.node.Node;
import election.statemachine.StateMachine;
import rpc.NodeEndpoint;

public class NodeMock implements Node {
    private StateMachine stateMachine;
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
        return true;
    }

    @Override
    public NodeEndpoint getLeaderNodeEndpoint() {
        return null;
    }

    @Override
    public void registerStateMachine(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }
}
