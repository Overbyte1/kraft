package server.config;

import org.junit.Test;

import java.io.*;
import java.util.Properties;

import static org.junit.Assert.*;

public class ServerConfigLoaderTest {
    @Test
    public void load() throws IOException {
        ConfigLoader loader = new ServerConfigLoader();
        Properties properties = new Properties();
        properties.setProperty("server_port", "8884");
        properties.setProperty("execution_timeout", "9999");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        properties.store(outputStream, "");
        ServerConfig config = loader.load(new ByteArrayInputStream(outputStream.toByteArray()));
        assertEquals(8884, config.getPort());
        assertEquals(9999, config.getExecuteTimeout());

    }
    @Test
    public void testFile() throws IOException {
        ConfigLoader loader = new ServerConfigLoader();

        InputStream inputStream =  this.getClass().getClassLoader().getResourceAsStream("server.properties");
        assert inputStream != null;
        ServerConfig config = loader.load(inputStream);
        assertEquals(8848, config.getPort());
        assertEquals(3000, config.getExecuteTimeout());

    }
}