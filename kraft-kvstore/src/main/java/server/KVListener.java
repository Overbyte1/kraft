package server;

import common.message.Connection;

public interface KVListener {
    void listen(Connection<?> o);
}
