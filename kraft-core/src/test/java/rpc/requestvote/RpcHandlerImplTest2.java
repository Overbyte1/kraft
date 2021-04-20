package rpc.requestvote;

import com.alibaba.fastjson.JSON;
import config.ClusterConfig;
import config.DefaultConfigLoader;
import election.config.GlobalConfig;
import election.node.*;
import election.statemachine.DefaultStateMachine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rpc.ChannelGroup;
import rpc.Endpoint;
import rpc.NodeEndpoint;
import rpc.RpcHandlerImpl;
import schedule.SingleThreadTaskScheduler;
import schedule.TaskScheduler;

import java.io.FileInputStream;
import java.io.IOException;

public class RpcHandlerImplTest2 {
    private Node node;
//    private int port;
//    private ChannelGroup channelGroup;
//    private RpcHandlerImpl rpcHandler;
//    private NodeImpl node;
//    private ClusterConfig config;
//    @Before
//    public void init() throws IOException {
//        config = new DefaultConfigLoader().load(null);
//        port = 8092;
//        channelGroup = new ChannelGroup(new NodeId("C"));
//        NodeGroup nodeGroup = initNodeGroup();
//        rpcHandler = new RpcHandlerImpl(channelGroup, port);
//        TaskScheduler scheduleExecutor = new SingleThreadTaskScheduler(config.getMinElectionTimeout(),
//                config.getMaxElectionTimeout(), config.getLogReplicationResultTimeout());
//
//        node = new NodeImpl(null, nodeGroup, rpcHandler, scheduleExecutor, config);
//    }
//    private NodeGroup initNodeGroup() {
//        NodeGroup nodeGroup = new NodeGroup();
//        NodeId nodeId1 = new NodeId("A");
//        GroupMember member1 = new GroupMember(new ReplicationState(0, 0),
//                new NodeEndpoint(nodeId1, new Endpoint("localhost", 8090)));
//        NodeId nodeId2 = new NodeId("B");
//        GroupMember member2 = new GroupMember(new ReplicationState(0, 0),
//                new NodeEndpoint(nodeId2, new Endpoint("localhost", 8091)));
//        nodeGroup.addGroupMember(nodeId1, member1);
//        nodeGroup.addGroupMember(nodeId2, member2);
//        return nodeGroup;
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
        Thread.sleep(10000000);
    }

    public void testSendAppendEntriesMessage() {
    }

    public void testSendRequestVoteResultMessage() {
    }

    public void testSendAppendEntriesResultMessage() {
    }
}