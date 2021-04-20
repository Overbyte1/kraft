package client.config;

import client.balance.LoadBalancePolicy;

public class ClientConfig {
    private int serverPort;
    private String serverIp;
    private long connectTimeout;
    private long sendTimeout;
    private int loadBalancePolicy;

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public long getSendTimeout() {
        return sendTimeout;
    }

    public void setSendTimeout(long sendTimeout) {
        this.sendTimeout = sendTimeout;
    }

    public int getLoadBalancePolicy() {
        return loadBalancePolicy;
    }

    public void setLoadBalancePolicy(int loadBalancePolicy) {
        this.loadBalancePolicy = loadBalancePolicy;
    }
}
