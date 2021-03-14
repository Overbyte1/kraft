package election.nodeimpl;

import election.config.GlobalConfig;
import log.Log;
import log.LogImpl;
import log.store.LogStore;
import log.store.MemoryLogStore;
import election.node.GroupMember;
import election.node.NodeGroup;
import election.node.NodeId;
import election.node.NodeImpl;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

public class NodeImplTest1 {
    private static final Logger logger = LoggerFactory.getLogger(RpcHandlerImplTest.class);
    private int selfPort;
    private NodeId selfNodeId;
    private ChannelGroup channelGroup;
    private RpcHandlerImpl rpcHandler;
    private NodeImpl node;
    private LogStore logStore;
    private StateMachine stateMachine;
    private NodeGroup nodeGroup;
    private Log defaultLog;
    @Before
    public void initNodeGroup() throws IOException {
        nodeGroup = new NodeGroup();

        int seq = 2, memberNum = 5;
        String commonPrefix = "node", idSuffix = "_nodeId", ipSuffix = "_ip", portSuffix = "_port";
        String selfIdName = "self_id", selfPortName = "self_port";
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("cluster-member2.properties");
        Properties properties = new Properties();
        properties.load(inputStream);

        selfNodeId = new NodeId(properties.getProperty(selfIdName));
        selfPort = Integer.parseInt(properties.getProperty(selfPortName));
        //nodeGroup
        for(int i = 1; i <= memberNum; i++) {
            if(i == seq) continue;
            String nodeKey = commonPrefix + i + idSuffix;
            String nodeIdstr = properties.getProperty(nodeKey);
            NodeId nodeId = new NodeId(nodeIdstr);

            String ipKey = commonPrefix + i + ipSuffix;
            String ip = properties.getProperty(ipKey);

            String portKey = commonPrefix + i + portSuffix;
            int port = Integer.parseInt(properties.getProperty(portKey));

            NodeEndpoint nodeEndpoint = new NodeEndpoint(nodeId, new Endpoint(ip, port));
            GroupMember member = new GroupMember(nodeEndpoint);

            nodeGroup.addGroupMember(nodeId, member);

        }
        Collection<GroupMember> allGroupMember = nodeGroup.getAllGroupMember();
        for (GroupMember member : allGroupMember) {
            System.out.println(member);
        }

    }
    @Before
    public void initOther() {
        channelGroup = new ChannelGroup(selfNodeId);
        logStore = new MemoryLogStore();
        stateMachine = null;
        rpcHandler = new RpcHandlerImpl(channelGroup, selfPort);
        defaultLog = new LogImpl(logStore, stateMachine, 0, nodeGroup);
        node = new NodeImpl(nodeGroup, rpcHandler, new SingleThreadTaskScheduler(), defaultLog,
                new GlobalConfig(), selfNodeId);

    }
    @Test
    public void testLog() {
        rpcHandler.initialize();
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
}
