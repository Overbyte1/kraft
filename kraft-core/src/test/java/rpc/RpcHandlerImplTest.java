package rpc;

import election.config.GlobalConfig;
import election.node.*;
import election.role.AbstractRole;
import election.role.FollowerRole;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schedule.SingleTaskScheduleExecutor;
import schedule.TaskScheduleExecutor;

public class RpcHandlerImplTest {
    private static final Logger logger = LoggerFactory.getLogger(RpcHandlerImplTest.class);
    private int port;
    private ChannelGroup channelGroup;
    private RpcHandler rpcHandler;
    private NodeImpl node;
    @Before
    public void init() {
        port = 8090;
        NodeId nodeId = new NodeId("A");
        channelGroup = new ChannelGroup(nodeId);
        NodeGroup nodeGroup = initNodeGroup();
        rpcHandler = new RpcHandlerImpl(channelGroup, port);
        TaskScheduleExecutor scheduleExecutor = new SingleTaskScheduleExecutor();
        GlobalConfig config = new GlobalConfig();
        AbstractRole role = new FollowerRole(nodeId, 0);
        node = new NodeImpl(role, nodeGroup, rpcHandler, scheduleExecutor, config);
    }
    private NodeGroup initNodeGroup() {
        NodeGroup nodeGroup = new NodeGroup();
        NodeId nodeId1 = new NodeId("B");
        GroupMember member1 = new GroupMember(new ReplicationState(0, 0),
                new NodeEndpoint(nodeId1, new Endpoint("localhost", 8091)));
        NodeId nodeId2 = new NodeId("C");
        GroupMember member2 = new GroupMember(new ReplicationState(0, 0),
                new NodeEndpoint(nodeId2, new Endpoint("localhost", 8092)));

        nodeGroup.addGroupMember(nodeId1, member1);
        nodeGroup.addGroupMember(nodeId2, member2);
        return nodeGroup;
    }
    @Test
    public void testSendRequestVoteMessage() throws InterruptedException {
        rpcHandler.initialize();
        node.start();
        Thread.sleep(10000000);
    }
    @After
    public void waitThread() throws InterruptedException {

    }

    public void testSendAppendEntriesMessage() {
    }

    public void testSendRequestVoteResultMessage() {
    }

    public void testSendAppendEntriesResultMessage() {
    }

}