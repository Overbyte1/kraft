package config;

import election.node.NodeId;
import rpc.NodeEndpoint;

import java.util.List;

public class ClusterConfig {
    //network
    private int minElectionTimeout = 6000;
    private int maxElectionTimeout = 10000;
    private int connectTimeout = 800;
    private int port;
    private String selfIp;

    //log
    private int logReplicationInterval = 1000;
    private int logReplicationResultTimeout = 4000;
    private int maxTransportEntries = -1;
    private String path;

    //members
    private List<NodeEndpoint> members;
    private NodeId selfId;


    public String getSelfIp() {
        return selfIp;
    }

    public void setSelfIp(String selfIp) {
        this.selfIp = selfIp;
    }

    public NodeId getSelfId() {
        return selfId;
    }

    public void setSelfId(NodeId selfId) {
        this.selfId = selfId;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public List<NodeEndpoint> getMembers() {
        return members;
    }

    public void setMembers(List<NodeEndpoint> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ClusterConfig{");
        sb.append("minElectionTimeout=").append(minElectionTimeout);
        sb.append(", maxElectionTimeout=").append(maxElectionTimeout);
        sb.append(", connectTimeout=").append(connectTimeout);
        sb.append(", port=").append(port);
        sb.append(", selfIp='").append(selfIp).append('\'');
        sb.append(", logReplicationInterval=").append(logReplicationInterval);
        sb.append(", logReplicationResultTimeout=").append(logReplicationResultTimeout);
        sb.append(", maxTransportEntries=").append(maxTransportEntries);
        sb.append(", path='").append(path).append('\'');
        sb.append(", members=").append(members);
        sb.append(", selfId=").append(selfId);
        sb.append('}');
        return sb.toString();
    }
}
