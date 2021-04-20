package election;

import com.alibaba.fastjson.JSON;
import config.ClusterConfig;
import election.node.Node;
import election.node.NodeId;
import election.node.NodeImpl;
import election.statemachine.DefaultStateMachine;
import org.junit.Test;
import rpc.Endpoint;
import rpc.NodeEndpoint;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class NodeBuilderTest {
    @Test
    public void testBuild() throws IOException, InterruptedException {
        NodeImpl.NodeBuilder builder = NodeImpl.builder();
        List<NodeEndpoint> list = new ArrayList<>();
        list.add(new NodeEndpoint(new NodeId("A"), new Endpoint("localhost1", 8988)));
        Node node = builder.withId("A")
                .withListenPort(8090)
                .withPath("./data/node11/")
                .withStateMachine(new DefaultStateMachine())
                .withElectionTimeout(1000, 3000)
                .withNodeList(list)
                .withLogReplicationInterval(300)
                .withElectionTimeout(5000, 10000)
                .build();
        node.start();
    }
    @Test
    public void testBuildFromConfig() throws IOException, InterruptedException {
        ClusterConfig config = JSON.parseObject(new FileInputStream("./conf/raft.json"), ClusterConfig.class);
        assertEquals(6000, config.getMinElectionTimeout());
        assertEquals(9000, config.getMaxElectionTimeout());

        NodeImpl.NodeBuilder builder = NodeImpl.builder();
        Node node = builder.justBuild(config, new DefaultStateMachine());
        node.start();
    }
}
