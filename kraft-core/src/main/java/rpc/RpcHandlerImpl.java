package rpc;

import election.node.NodeId;
import election.log.LogEntry;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import rpc.message.AbstractMessage;
import rpc.message.RequestVoteMessage;

import java.util.List;

public class RpcHandlerImpl implements RpcHandler {
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    private int port;


    @Override
    public void initialize() {

    }

    @Override
    public void sendRequestVoteMessage(long term, NodeId candidateId, long lastLogIndex, long lastLogTerm) {
        AbstractMessage<RequestVoteMessage> message = new AbstractMessage<>(0,
                new RequestVoteMessage(term, candidateId, lastLogIndex, lastLogTerm));

    }

    @Override
    public void sendAppendEntriesMessage(long term, NodeId leaderId, long preLogIndex, long preLogTerm,
                                         List<LogEntry> logEntryList, long leaderCommit) {

    }

    @Override
    public void sendRequestVoteResultMessage(long term, boolean voteGranted) {

    }

    @Override
    public void sendAppendEntriesResultMessage(long term, boolean success) {

    }
}
