package server.config;

public class ServerConfig {
    private int port;
    private int intervalPort;
    private int analysisPort;
    private long executeTimeout;

    public ServerConfig(){}

    public ServerConfig(int port, long executeTimeout) {
        this.port = port;
        this.executeTimeout = executeTimeout;
    }

    public int getIntervalPort() {
        return intervalPort;
    }

    public void setIntervalPort(int intervalPort) {
        this.intervalPort = intervalPort;
    }

    public int getAnalysisPort() {
        return analysisPort;
    }

    public void setAnalysisPort(int analysisPort) {
        this.analysisPort = analysisPort;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getExecuteTimeout() {
        return executeTimeout;
    }

    public void setExecuteTimeout(long executeTimeout) {
        this.executeTimeout = executeTimeout;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ServerConfig{");
        sb.append("port=").append(port);
        sb.append(", intervalPort=").append(intervalPort);
        sb.append(", analysisPort=").append(analysisPort);
        sb.append(", executeTimeout=").append(executeTimeout);
        sb.append('}');
        return sb.toString();
    }
}
