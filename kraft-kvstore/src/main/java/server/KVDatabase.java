package server;

import common.message.Connection;
import common.message.command.DelCommand;
import common.message.command.GetCommand;
import common.message.command.SetCommand;

import java.io.IOException;

public interface KVDatabase {
    void start();
    void stop();
    void handleGetCommand(Connection<GetCommand> connection);
    void handleSetCommand(Connection<SetCommand> connection) throws IOException;
    void handleDelCommand(Connection<DelCommand> connection) throws IOException;
}
