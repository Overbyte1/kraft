package client;

import client.balance.LoadBalance;
import client.config.ClientConfig;
import client.handler.InlineCommandHandler;
import election.node.NodeId;
import rpc.Endpoint;

import java.util.Map;

public class CommandContext {
    private boolean running;
    private final Router router;
    private final LoadBalance loadBalance;
    private final ClientConfig config;
    private final Map<String, InlineCommandHandler> inlineCommandHandlerMap;

    public CommandContext(Map<NodeId, Endpoint> serverMap, LoadBalance loadBalance, ClientConfig config, Map<String,
            InlineCommandHandler> inlineCommandHandlerMap) {
        running = false;
        this.router = new Router(serverMap);
        this.loadBalance = loadBalance;
        this.config = config;
        this.inlineCommandHandlerMap = inlineCommandHandlerMap;
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

    public Map<String, InlineCommandHandler> getInlineCommandHandlerMap() {
        return inlineCommandHandlerMap;
    }

    public ClientConfig getConfig() {
        return config;
    }
}
