package config;

import com.alibaba.fastjson.JSON;
import election.node.NodeId;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ClusterConfigTest {
    @Test
    public void testConfig() throws IOException, InterruptedException {
        ClusterConfig config = JSON.parseObject(new FileInputStream("./conf/raft.json"), ClusterConfig.class);
        System.out.println(config);
        Thread.sleep(1000000);
    }
    @Test
    public void testOne() throws InterruptedException {
        List<NodeId> nodeIds = new LinkedList<>();
        nodeIds.add(new NodeId("A"));
        List<NodeId> nodeIds1 = new LinkedList<>(nodeIds);
        assert nodeIds.get(0) == nodeIds1.get(0);
        Thread.sleep(1000000);
    }
}