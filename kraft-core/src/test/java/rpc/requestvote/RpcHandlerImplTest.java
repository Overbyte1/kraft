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

    @Before
    public void builder() throws IOException {
        ClusterConfig config = JSON.parseObject(new FileInputStream("./conf/raft1.json"), ClusterConfig.class);
        NodeImpl.NodeBuilder builder = NodeImpl.builder();
        node = builder.withId("A")
                .withListenPort(config.getPort())
                .withLogReplicationInterval(config.getLogReplicationInterval())
                .withNodeList(config.getMembers())
                .withMemLogStore()
                .withStateMachine(new DefaultStateMachine())
                .build();
//        node = builder.justBuild(config, new DefaultStateMachine());
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