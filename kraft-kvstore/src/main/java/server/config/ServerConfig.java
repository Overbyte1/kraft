package server.config;

public class ServerConfig {
    private int port;
    private long executeTimeout;

    public ServerConfig(){}

    public ServerConfig(int port, long executeTimeout) {
        this.port = port;
        this.executeTimeout = executeTimeout;
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
        sb.append(", executeTimeout=").append(executeTimeout);
        sb.append('}');
        return sb.toString();
    }
}
