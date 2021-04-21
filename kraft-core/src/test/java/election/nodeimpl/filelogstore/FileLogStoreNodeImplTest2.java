package election.nodeimpl.filelogstore;

import com.alibaba.fastjson.JSON;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FileLogStoreNodeImplTest2 {
    private Node node;
    @Before
    public void builder() throws IOException {
        ClusterConfig config = JSON.parseObject(new FileInputStream("./conf/raft3.json"), ClusterConfig.class);
        NodeImpl.NodeBuilder builder = NodeImpl.builder();
        node = builder.withId("C")
                .withListenPort(config.getPort())
                .withLogReplicationInterval(config.getLogReplicationInterval())
                .withNodeList(config.getMembers())
                .withPath(config.getPath() + "C/")
                .withStateMachine(new DefaultStateMachine())
                .build();
    }
    @Test
    public void testLog() throws InterruptedException {
        //rpcHandler.initialize();
        node.start();
        Thread.sleep(10000);
        if(node.isLeader()) {
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(() -> {
                node.appendLog(new byte[]{0, 1, 2, 3, 4});
            }, 0, 5, TimeUnit.SECONDS);
        }
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
