package log;

import election.node.GroupMember;
import election.node.NodeGroup;
import election.node.NodeId;
import election.statemachine.StateMachine;
import javafx.scene.Node;
import log.store.FileLogStore;
import log.store.MemoryLogStore;
import org.junit.Test;
import rpc.Endpoint;
import rpc.NodeEndpoint;

import java.util.Arrays;

import static org.junit.Assert.*;

public class LogImplTest {

    @Test
    public void advanceCommitForLeader() throws InterruptedException {
        MemoryLogStore logStore = new MemoryLogStore();
        NodeGroup nodeGroup = new NodeGroup();
        NodeId nodeId = new NodeId("A");
        Endpoint endpoint = new Endpoint("localhost", 8888);
        nodeGroup.addGroupMember(nodeId, new GroupMember(new NodeEndpoint(nodeId, endpoint)));
        LogImpl log = new LogImpl(logStore, new StateMachine() {
            @Override
            public boolean apply(byte[] command) {
                System.out.println("state machine apply: " + Arrays.toString(command));
                return false;
            }
        }, nodeGroup);

        log.appendGeneralEntry(1, new byte[]{9,9});

        Thread.sleep(100000);
    }

    @Test
    public void getLastLogIndex() {
    }
}