package config;

import com.alibaba.fastjson.JSON;
import election.node.NodeId;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;

public class ClusterConfigTest {
    @Test
    public void testConfig() throws IOException, InterruptedException {
        ClusterConfig config = JSON.parseObject(new FileInputStream("./conf/raft.json"), ClusterConfig.class);
        System.out.println(config);
        Thread.sleep(1000000);
    }
    @Test
    public void testOne() throws InterruptedException {
        Thread.sleep(1000000);
    }
}