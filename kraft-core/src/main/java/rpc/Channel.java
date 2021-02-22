package rpc;

import rpc.message.AppendEntriesMessage;
import rpc.message.AppendEntriesResultMessage;
import rpc.message.RequestVoteMessage;
import rpc.message.RequestVoteResultMessage;

public interface Channel {
    void writeRequestVoteMessage(RequestVoteMessage message);

    void writeAppendEntriesMessage(AppendEntriesMessage message);

    void writeRequestVoteResultMessage(RequestVoteResultMessage message);

    void writeAppendEntriesResultMessage(AppendEntriesResultMessage message);

    void close();
}
