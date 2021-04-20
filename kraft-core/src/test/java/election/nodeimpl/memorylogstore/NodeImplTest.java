package election.nodeimpl.memorylogstore;

import com.alibaba.fastjson.JSON;
import config.ClusterConfig;
import config.DefaultConfigLoader;
import election.config.GlobalConfig;
import election.node.*;
import election.statemachine.DefaultStateMachine;
import log.Log;
import log.LogImpl;
import log.store.LogStore;
import log.store.MemoryLogStore;
import election.statemachine.StateMachine;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.ChannelGroup;
import rpc.Endpoint;
import rpc.NodeEndpoint;
import rpc.RpcHandlerImpl;
import rpc.requestvote.RpcHandlerImplTest;
import schedule.SingleThreadTaskScheduler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.Semaphore;

public class NodeImplTest {
    private Node node;
//    private static final Logger logger = LoggerFactory.getLogger(RpcHandlerImplTest.class);
//    private int selfPort;
//    private NodeId selfNodeId;
//    private ChannelGroup channelGroup;
//    private RpcHandlerImpl rpcHandler;
//    private NodeImpl node;
//    private LogStore logStore;
//    private StateMachine stateMachine;
//    private NodeGroup nodeGroup;
//    private Log defaultLog;
//    @Before
//    public void initNodeGroup() throws IOException {
//        nodeGroup = new NodeGroup();
//
//        int seq = 1, memberNum = 5;
//        String commonPrefix = "node", idSuffix = "_nodeId", ipSuffix = "_ip", portSuffix = "_port";
//        String selfIdName = "self_id", selfPortName = "self_port";
//        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("cluster-member1.properties");
//        Properties properties = new Properties();
//        properties.load(inputStream);
//
//        selfNodeId = new NodeId(properties.getProperty(selfIdName));
//        selfPort = Integer.parseInt(properties.getProperty(selfPortName));
//        //nodeGroup
//        for(int i = 1; i <= memberNum; i++) {
//            if(i == seq) continue;
//            String nodeKey = commonPrefix + i + idSuffix;
//            String nodeIdstr = properties.getProperty(nodeKey);
//            NodeId nodeId = new NodeId(nodeIdstr);
//
//            String ipKey = commonPrefix + i + ipSuffix;
//            String ip = properties.getProperty(ipKey);
//
//            String portKey = commonPrefix + i + portSuffix;
//            int port = Integer.parseInt(properties.getProperty(portKey));
//
//            NodeEndpoint nodeEndpoint = new NodeEndpoint(nodeId, new Endpoint(ip, port));
//            GroupMember member = new GroupMember(nodeEndpoint);
//
//            nodeGroup.addGroupMember(nodeId, member);
//
//        }
//        Collection<GroupMember> allGroupMember = nodeGroup.getAllGroupMember();
//        for (GroupMember member : allGroupMember) {
//            System.out.println(member);
//        }
//
//    }
//    @Before
//    public void initOther() throws IOException {
//        System.out.println("init others");
//        ClusterConfig config = new DefaultConfigLoader().load(null);
//        System.out.println(config);
//        channelGroup = new ChannelGroup(selfNodeId);
//        logStore = new MemoryLogStore();
//        stateMachine = null;
//        rpcHandler = new RpcHandlerImpl(channelGroup, selfPort, config.getConnectTimeout());
//        defaultLog = new LogImpl(logStore, stateMachine, 0, nodeGroup);
//        SingleThreadTaskScheduler scheduler = new SingleThreadTaskScheduler(config.getMinElectionTimeout(),
//                config.getMaxElectionTimeout(), config.getLogReplicationResultTimeout());
//        node = new NodeImpl(nodeGroup, rpcHandler, scheduler, defaultLog, config, selfNodeId);
//
//
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
    public void testLog() {

        node.start();
        waiting();
    }
    private void waiting() {
        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testU() throws InterruptedException {
        Semaphore semaphore = null;
    }
    private int fun(int a, int b) {
        return 0;
    }
}
