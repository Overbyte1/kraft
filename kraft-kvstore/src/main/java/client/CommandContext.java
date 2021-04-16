package client;

import client.balance.LoadBalance;
import election.node.NodeId;
import rpc.Endpoint;

import java.util.Map;

public class CommandContext {
    private boolean running;
    private final Router router;
    private final LoadBalance loadBalance;

    public CommandContext(Map<NodeId, Endpoint> serverMap, LoadBalance loadBalance) {
        running = false;
        this.router = new Router(serverMap);
        this.loadBalance = loadBalance;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
    public Router getRouter() {
        return router;
    }

    public LoadBalance getLoadBalance() {
        return loadBalance;
    }
}
