package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DefaultConfigLoader implements ConfigLoader {
    private static final int DEFAULT_MIN_ELECTION_TIMEOUT = 6000;
    private static final int DEFAULT_MAX_ELECTION_TIMEOUT = 10000;
    private static final int DEFAULT_lOG_REPLICATION_INTERVAL = 1000;
    private static final int DEFAULT_lOG_REPLICATION_RESULT_TIMEOUT = 4000;
    private static final int DEFAULT_CONNECT_TIMEOUT = 800;
    private static final int DEFAULT_MAX_TRANSPORT_ENTRIES = -1;
    private static final String DEFAULT_PATH = "./data/";
    private static final int DEFAULT_PORT = 8888;

    private static final Logger logger = LoggerFactory.getLogger(DefaultConfigLoader.class);

    private final String propertyNamePrefix;

    public DefaultConfigLoader() {
        this("");
    }

    public DefaultConfigLoader(String propertyNamePrefix) {
        this.propertyNamePrefix = propertyNamePrefix;
    }

    @Override
    public ClusterConfig load(InputStream inputStream) throws IOException {
        Properties p = new Properties();
        if(inputStream != null)
            p.load(inputStream);

        ClusterConfig config = new ClusterConfig();
        config.setMinElectionTimeout(getIntProperty(p, "election.timeout.min", DEFAULT_MIN_ELECTION_TIMEOUT));
        assert(config.getMinElectionTimeout() > 0);

        config.setMaxElectionTimeout(getIntProperty(p, "election.timeout.max", DEFAULT_MAX_ELECTION_TIMEOUT));
        assert(config.getMaxElectionTimeout() > 0);

        assert(config.getMinElectionTimeout() <= config.getMaxElectionTimeout());

        config.setLogReplicationInterval(getIntProperty(p, "replication.interval", DEFAULT_lOG_REPLICATION_INTERVAL));
        assert(config.getLogReplicationInterval() > 0);

        config.setLogReplicationResultTimeout(getIntProperty(p, "replication.interval.result.timeout", DEFAULT_lOG_REPLICATION_RESULT_TIMEOUT));
        assert(config.getLogReplicationResultTimeout() > 0);

        config.setConnectTimeout(getIntProperty(p, "network.timeout.connect", DEFAULT_CONNECT_TIMEOUT));
        assert(config.getConnectTimeout() > 0);

        config.setMaxTransportEntries(getIntProperty(p, "log.transport.entry.max", DEFAULT_MAX_TRANSPORT_ENTRIES));
        assert(config.getMaxTransportEntries() >= -1 && config.getMaxTransportEntries() != 0);

        config.setPath(getStringProperty(p, "log.data.path", DEFAULT_PATH));

        config.setPort(getIntProperty(p, "network.port", DEFAULT_PORT));
        assert(config.getPort() > 0);

        return config;
    }

    private int getIntProperty(Properties properties, String name, int defaultValue) {
        try {
            String value = properties.getProperty(propertyNamePrefix + name);
            if (value != null) {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    logger.warn("illegal value [" + value + "] for property " + name +
                            ", fallback to default value " + defaultValue);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return defaultValue;
    }
    private String getStringProperty(Properties properties, String name, String defaultValue) {
        String value = properties.getProperty(name);
        if(value == null || "".equals(value)) {
            return defaultValue;
        }
        return value;
    }
}
