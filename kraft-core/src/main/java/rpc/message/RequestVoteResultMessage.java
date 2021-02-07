package rpc.message;

public class RequestVoteResultMessage {
    private long term;
    private boolean voteGranted;

    public RequestVoteResultMessage(long term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }

}
