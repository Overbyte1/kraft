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

    @Before
    public void builder() throws IOException {
        ClusterConfig config = JSON.parseObject(new FileInputStream("./conf/raft3.json"), ClusterConfig.class);
        NodeImpl.NodeBuilder builder = NodeImpl.builder();
        node = builder.withId("C")
                .withListenPort(config.getPort())
                .withNodeList(config.getMembers())
                .withLogReplicationInterval(config.getLogReplicationInterval())
                .withMemLogStore()
                .withStateMachine(new DefaultStateMachine())
                .build();
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