package server;

import common.message.Connection;
import common.message.DelCommand;
import common.message.GetCommand;
import common.message.SetCommand;

import java.io.IOException;

public interface KVStore {
    void start();
    void handleGetCommand(Connection<GetCommand> connection);
    void handleSetCommand(Connection<SetCommand> connection) throws IOException;
    void handleDelCommand(Connection<DelCommand> connection) throws IOException;
}
