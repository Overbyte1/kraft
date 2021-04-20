package rpc.requestvote;

import com.alibaba.fastjson.JSON;
import config.ClusterConfig;
import config.DefaultConfigLoader;
import election.config.GlobalConfig;
import election.statemachine.DefaultStateMachine;
import log.DefaultLog;
import log.Log;
import log.LogImpl;
import log.store.LogStore;
import log.store.MemoryLogStore;
import election.node.*;
import election.role.AbstractRole;
import election.role.FollowerRole;
import election.statemachine.StateMachine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.ChannelGroup;
import rpc.Endpoint;
import rpc.NodeEndpoint;
import rpc.RpcHandlerImpl;
import schedule.SingleThreadTaskScheduler;
import schedule.TaskScheduler;

import java.io.FileInputStream;
import java.io.IOException;

public class RpcHandlerImplTest {
    private Node node;
//    private static final Logger logger = LoggerFactory.getLogger(RpcHandlerImplTest.class);
//    private int port;
//    private ChannelGroup channelGroup;
//    private RpcHandlerImpl rpcHandler;
//    private NodeImpl node;
//    private LogStore logStore;
//    private StateMachine stateMachine;
//    private NodeGroup nodeGroup;
//    private DefaultLog defaultLog;
//    @Before
//    public void init() throws IOException {
//        port = 8090;
//        NodeId nodeId = new NodeId("A");
//        channelGroup = new ChannelGroup(nodeId);
//        NodeGroup nodeGroup = initNodeGroup();
//        rpcHandler = new RpcHandlerImpl(channelGroup, port);
//        //TaskScheduleExecutor scheduleExecutor = new SingleTaskScheduleExecutor();
//        ClusterConfig config = new DefaultConfigLoader().load(null);
//        TaskScheduler scheduleExecutor = new SingleThreadTaskScheduler(config.getMinElectionTimeout(),
//                config.getMaxElectionTimeout(), config.getLogReplicationResultTimeout());
////        GlobalConfig config = new GlobalConfig();
//        AbstractRole role = new FollowerRole(nodeId, 0);
//        logStore = new MemoryLogStore();
//        stateMachine = null;
//        defaultLog = new DefaultLog(logStore, stateMachine, nodeGroup);
//        Log log = new LogImpl(logStore, stateMachine, 0, nodeGroup);
//        node = new NodeImpl(nodeGroup, rpcHandler, scheduleExecutor, log, config, nodeId);
//    }
//    private NodeGroup initNodeGroup() {
//        NodeGroup nodeGroup = new NodeGroup();
//        NodeId nodeId1 = new NodeId("B");
//        GroupMember member1 = new GroupMember(new ReplicationState(0, 0),
//                new NodeEndpoint(nodeId1, new Endpoint("localhost", 8091)));
//        NodeId nodeId2 = new NodeId("C");
//        GroupMember member2 = new GroupMember(new ReplicationState(0, 0),
//                new NodeEndpoint(nodeId2, new Endpoint("localhost", 8092)));
//        NodeId nodeId3 = new NodeId("D");
//        GroupMember member3 = new GroupMember(new ReplicationState(0, 0),
//                new NodeEndpoint(nodeId3, new Endpoint("localhost", 8093)));
//        NodeId nodeId4 = new NodeId("E");
//        GroupMember member4 = new GroupMember(new ReplicationState(0, 0),
//                new NodeEndpoint(nodeId4, new Endpoint("localhost", 8094)));
//
//        nodeGroup.addGroupMember(nodeId1, member1);
//        nodeGroup.addGroupMember(nodeId2, member2);
//        nodeGroup.addGroupMember(nodeId3, member3);
//        nodeGroup.addGroupMember(nodeId4, member4);
//        return nodeGroup;
//    }
//    private DefaultLog initLog(LogStore logStore, StateMachine stateMachine, NodeGroup nodeGroup) {
//        return new DefaultLog(logStore, stateMachine, nodeGroup);
//    }

    @Before
    public void builder() throws IOException {
        ClusterConfig config = JSON.parseObject(new FileInputStream("./conf/raft.json"), ClusterConfig.class);
        config.setPort(9991);
        config.setPath(config.getPath() + config.getSelfId().getValue());
        NodeImpl.NodeBuilder builder = NodeImpl.builder();
        node = builder.justBuild(config, new DefaultStateMachine());
    }
    @Test
    public void testSendRequestVoteMessage() throws InterruptedException {

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