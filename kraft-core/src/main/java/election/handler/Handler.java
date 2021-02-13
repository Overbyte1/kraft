package election.handler;

import rpc.message.AbstractRequest;

public interface Handler {
    void handle(AbstractRequest request);
}
