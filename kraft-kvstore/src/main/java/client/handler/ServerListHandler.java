package client.handler;

import client.CommandContext;
import client.Router;
import election.node.NodeId;
import rpc.NodeEndpoint;

import java.util.Comparator;
import java.util.List;

public class ServerListHandler implements CommandHandler {
    @Override
    public String getCommandName() {
        return "serverlist";
    }

    @Override
    public void execute(String[] args, CommandContext commandContext) {
        Router router = commandContext.getRouter();
        List<NodeEndpoint> serverList = router.getServerList();
        boolean hasLeader = router.hasLeader();
        NodeId nodeId = router.getLeaderId();
        serverList.sort(Comparator.comparing(NodeEndpoint::getNodeId));
        for (NodeEndpoint nodeEndpoint : serverList) {
            System.out.print(nodeEndpoint.getNodeId() + " " + nodeEndpoint.getEndpoint());
            if(hasLeader && nodeId.equals(nodeEndpoint.getNodeId())) {
                System.out.print(" (leader)");
            }
            System.out.println();
        }
    }
}
