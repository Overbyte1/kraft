package rpc.requestvote;

import election.config.GlobalConfig;
import election.log.DefaultLog;
import election.log.LogStore;
import election.log.MemoryLogStore;
import election.node.*;
import election.role.AbstractRole;
import election.role.FollowerRole;
import election.statemachine.StateMachine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.*;
import schedule.SingleTaskScheduleExecutor;
import schedule.TaskScheduleExecutor;

public class RpcHandlerImplTest {
    private static final Logger logger = LoggerFactory.getLogger(RpcHandlerImplTest.class);
    private int port;
    private ChannelGroup channelGroup;
    private RpcHandler rpcHandler;
    private NodeImpl node;
    private LogStore logStore;
    private StateMachine stateMachine;
    private NodeGroup nodeGroup;
    private DefaultLog defaultLog;
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
        logStore = new MemoryLogStore();
        stateMachine = null;
        defaultLog = new DefaultLog(logStore, stateMachine, nodeGroup);;
        node = new NodeImpl(nodeGroup, rpcHandler, scheduleExecutor, defaultLog, config, nodeId);
    }
    private NodeGroup initNodeGroup() {
        NodeGroup nodeGroup = new NodeGroup();
        NodeId nodeId1 = new NodeId("B");
        GroupMember member1 = new GroupMember(new ReplicationState(0, 0),
                new NodeEndpoint(nodeId1, new Endpoint("localhost", 8091)));
        NodeId nodeId2 = new NodeId("C");
        GroupMember member2 = new GroupMember(new ReplicationState(0, 0),
                new NodeEndpoint(nodeId2, new Endpoint("localhost", 8092)));
        NodeId nodeId3 = new NodeId("D");
        GroupMember member3 = new GroupMember(new ReplicationState(0, 0),
                new NodeEndpoint(nodeId3, new Endpoint("localhost", 8093)));
        NodeId nodeId4 = new NodeId("E");
        GroupMember member4 = new GroupMember(new ReplicationState(0, 0),
                new NodeEndpoint(nodeId4, new Endpoint("localhost", 8094)));

        nodeGroup.addGroupMember(nodeId1, member1);
        nodeGroup.addGroupMember(nodeId2, member2);
        nodeGroup.addGroupMember(nodeId3, member3);
        nodeGroup.addGroupMember(nodeId4, member4);
        return nodeGroup;
    }
    private DefaultLog initLog(LogStore logStore, StateMachine stateMachine, NodeGroup nodeGroup) {
        return new DefaultLog(logStore, stateMachine, nodeGroup);
    }
    @Test
    public void testSendRequestVoteMessage() throws InterruptedException {
        rpcHandler.initialize();
        node.start();
        Thread.sleep(10000000);
        //TODO:完善日志
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