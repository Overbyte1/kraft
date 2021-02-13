package rpc.message;

public class MessageType {
    public static final int RequestVote = 1;
    public static final int RequestVoteResult = 2;
    public static final int AppendEntries = 3;
    public static final int AppendEntriesResult = 4;

    public static boolean isRequestVoteType(int type) {
        return type == RequestVote;
    }
    public static boolean isRequestVoteResultType(int type) {
        return type == RequestVoteResult;
    }
    public static boolean isAppendEntriesType(int type) {
        return type == AppendEntries;
    }
    public static boolean isAppendEntriesResultType(int type) {
        return type == AppendEntriesResult;
    }
}
