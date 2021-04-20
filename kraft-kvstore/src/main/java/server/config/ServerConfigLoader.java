package server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerConfigLoader implements ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ServerConfigLoader.class);
    private static final int DEFAULT_PORT = 8848;
    private static final long DEFAULT_EXECUTION_TIMEOUT = 3000;

    private final String propertyNamePrefix;

    public ServerConfigLoader() {
        this("");
    }

    public ServerConfigLoader(String propertyNamePrefix) {
        this.propertyNamePrefix = propertyNamePrefix;
    }

    @Override
    public ServerConfig load(InputStream inputStream) throws IOException {
        Properties p = new Properties();
        if(inputStream != null)
            p.load(inputStream);
        ServerConfig config = new ServerConfig();
        config.setPort(getIntProperty(p, "server_port", DEFAULT_PORT));
        config.setExecuteTimeout(getLongProperty(p, "execution_timeout", DEFAULT_EXECUTION_TIMEOUT));
        assert(config.getExecuteTimeout() > 0);

        return config;
    }
    private int getIntProperty(Properties properties, String name, int defaultValue) {
        String value = properties.getProperty(propertyNamePrefix + name);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("illegal value [" + value + "] for property " + name +
                        ", fallback to default value " + defaultValue);
            }
        }
        return defaultValue;
    }
    private long getLongProperty(Properties properties, String name, long defaultValue) {
        String value = properties.getProperty(propertyNamePrefix + name);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                logger.warn("illegal value [" + value + "] for property " + name +
                        ", fallback to default value " + defaultValue);
            }
        }
        return defaultValue;
    }

}
