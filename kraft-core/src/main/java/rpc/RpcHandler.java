package rpc;

import election.log.LogEntry;
import election.node.NodeId;
import rpc.message.AppendEntriesMessage;
import rpc.message.AppendEntriesResultMessage;
import rpc.message.RequestVoteMessage;
import rpc.message.RequestVoteResultMessage;

import java.util.Collection;
import java.util.List;

public interface RpcHandler {
    void initialize();

    void sendRequestVoteMessage(RequestVoteMessage message, Collection<NodeEndpoint> nodeEndpoints);

    void sendAppendEntriesMessage(AppendEntriesMessage message, Collection<NodeEndpoint> nodeEndpoints);

    void sendRequestVoteResultMessage(RequestVoteResultMessage message, NodeEndpoint nodeEndpoint);

    void sendAppendEntriesResultMessage(AppendEntriesResultMessage message, NodeEndpoint nodeEndpoint);


}
