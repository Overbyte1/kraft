package client.balance;

import client.Router;
import client.SocketChannelImpl;
import common.message.command.LeaderCommand;
import common.message.response.Response;
import common.message.response.ResponseType;
import rpc.NodeEndpoint;

import java.util.List;

public class LeaderOnlyLoadBalance extends AbstractLoadBalance {
    private final Router router;

    public LeaderOnlyLoadBalance(Router router) {
        super(new SocketChannelImpl(), router);
        this.router = router;
    }

    @Override
    public Object send(Object msg) {
        if(router.getLeaderId() == null) {
            List<NodeEndpoint> serverList = router.getServerList();
            Response<NodeEndpoint> resp = (Response<NodeEndpoint>) doSend(serverList.get(0).getEndpoint(), new LeaderCommand());
            if(resp.getType() == ResponseType.SUCCEED) {
                router.setLeaderId(resp.getBody().getNodeId());
            } else {
                throw new NoLeaderException("no leader");
            }
        }
        return doSend(router.getServer(router.getLeaderId()), msg);
    }

    @Override
    public Object send(String ip, int port, Object msg) {
        return null;
    }

    @Override
    public void initRouter(NodeEndpoint[] nodeEndpoints) {

    }
}
