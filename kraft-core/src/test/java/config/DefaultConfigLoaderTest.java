package config;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.*;

public class DefaultConfigLoaderTest {

    @Test
    public void load() throws IOException {
        ConfigLoader loader = new DefaultConfigLoader();
        Properties properties = new Properties();
        properties.setProperty("election.timeout.min", "1000");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        properties.store(outputStream, "");
        ClusterConfig config = loader.load(new ByteArrayInputStream(outputStream.toByteArray()));
        assertEquals(1000, config.getMinElectionTimeout());
        System.out.println(config.toString());
    }
}