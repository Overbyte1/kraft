package config;

public class ClusterConfig {
    private int minElectionTimeout = 6000;
    private int maxElectionTimeout = 10000;
    private int logReplicationInterval = 1000;
    private int logReplicationResultTimeout = 4000;
    private int connectTimeout = 800;
    private int maxTransportEntries = -1;

    public int getMinElectionTimeout() {
        return minElectionTimeout;
    }

    public void setMinElectionTimeout(int minElectionTimeout) {
        this.minElectionTimeout = minElectionTimeout;
    }

    public int getMaxElectionTimeout() {
        return maxElectionTimeout;
    }

    public void setMaxElectionTimeout(int maxElectionTimeout) {
        this.maxElectionTimeout = maxElectionTimeout;
    }

    public int getLogReplicationInterval() {
        return logReplicationInterval;
    }

    public void setLogReplicationInterval(int logReplicationInterval) {
        this.logReplicationInterval = logReplicationInterval;
    }

    public int getLogReplicationResultTimeout() {
        return logReplicationResultTimeout;
    }

    public void setLogReplicationResultTimeout(int logReplicationResultTimeout) {
        this.logReplicationResultTimeout = logReplicationResultTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getMaxTransportEntries() {
        return maxTransportEntries;
    }

    public void setMaxTransportEntries(int maxTransportEntries) {
        this.maxTransportEntries = maxTransportEntries;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ClusterConfig{");
        sb.append("minElectionTimeout=").append(minElectionTimeout);
        sb.append(", maxElectionTimeout=").append(maxElectionTimeout);
        sb.append(", logReplicationInterval=").append(logReplicationInterval);
        sb.append(", logReplicationResultTimeout=").append(logReplicationResultTimeout);
        sb.append(", connectTimeout=").append(connectTimeout);
        sb.append(", maxTransportEntries=").append(maxTransportEntries);
        sb.append('}');
        return sb.toString();
    }
}
