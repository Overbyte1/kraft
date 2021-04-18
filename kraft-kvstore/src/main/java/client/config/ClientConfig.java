package server.config;

public class ServerConfig {
    private int port;
    private long executeTimeout;

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
}
