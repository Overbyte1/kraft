package election.config;
//参数，用于测试
public class GlobalConfig {
    //单位是毫秒
    private int minElectionTimeout = 3000;
    private int maxElectionTimeout = 4000;
    private int logReplicationInterval = 600;
    private int logReplicationResultTimeout = 1200;
    private int connectTimeout = 800;

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

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
}
