package client.config;

import client.balance.LoadBalancePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClientConfigLoader implements ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ClientConfigLoader.class);
    private static final long DEFAULT_CONNECT_TIMEOUT = 5000;
    private static final long DEFAULT_SEND_TIMEOUT = 3000;
    private static final int DEFAULT_LOAD_BALANCE_POLICY = LoadBalancePolicy.POLLING;
    private static final int DEFAULT_SERVER_PORT = 8848;

    private final String propertyNamePrefix;

    public ClientConfigLoader() {
        this("");
    }

    public ClientConfigLoader(String propertyNamePrefix) {
        this.propertyNamePrefix = propertyNamePrefix;
    }

    @Override
    public ClientConfig load(InputStream inputStream) throws IOException {
        Properties p = new Properties();
        if(inputStream != null)
            p.load(inputStream);
        ClientConfig config = new ClientConfig();
        config.setConnectTimeout(getLongProperty(p, "connect_timeout", DEFAULT_CONNECT_TIMEOUT));
        assert(config.getConnectTimeout() > 0);

        config.setSendTimeout(getLongProperty(p, "send_timeout", DEFAULT_SEND_TIMEOUT));
        assert(config.getSendTimeout() > 0);

        config.setLoadBalancePolicy(getIntProperty(p, "load_balance_policy", DEFAULT_LOAD_BALANCE_POLICY));

        config.setServerIp(p.getProperty("server_ip"));
        assert(!"".equals(config.getServerIp()));

        config.setServerPort(getIntProperty(p, "server_port", DEFAULT_SERVER_PORT));
        assert(config.getServerPort() > 0);

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
