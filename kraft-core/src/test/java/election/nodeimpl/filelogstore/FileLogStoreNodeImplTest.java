package election.nodeimpl.filelogstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import config.ClusterConfig;
import config.DefaultConfigLoader;
import election.config.GlobalConfig;
import election.node.*;
import election.statemachine.DefaultStateMachine;
import election.statemachine.StateMachine;
import log.Log;
import log.LogImpl;
import log.store.FileLogStore;
import log.store.LogStore;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.*;

public class FileLogStoreNodeImplTest {
    private static final Logger logger = LoggerFactory.getLogger(RpcHandlerImplTest.class);
    private Node node;
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
//        id = properties.getProperty(selfIdName);
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
//        config = new DefaultConfigLoader().load(null);
//        channelGroup = new ChannelGroup(selfNodeId);
//        //替换成FileLogStore
//        logStore = new FileLogStore(path);
//        stateMachine = null;
//        rpcHandler = new RpcHandlerImpl(channelGroup, selfPort, config.getConnectTimeout());
//        defaultLog = new LogImpl(logStore, stateMachine, 0, nodeGroup);
//        SingleThreadTaskScheduler scheduler = new SingleThreadTaskScheduler(config.getMinElectionTimeout(),
//                config.getMaxElectionTimeout(), config.getLogReplicationResultTimeout());
//        node = new NodeImpl(nodeGroup, rpcHandler, scheduler, defaultLog, config, selfNodeId);
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
        //rpcHandler.initialize();
        node.start();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(()-> {
            node.appendLog(new byte[]{0, 1, 2, 3, 4});
        }, 0, 5, TimeUnit.SECONDS);
        waiting();
    }
    private void waiting() {
        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
