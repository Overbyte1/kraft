package server;

import common.message.Connection;
import common.message.command.DelCommand;
import common.message.command.GetCommand;
import common.message.command.SetCommand;
import server.handler.CommandHandler;

import java.io.IOException;

public interface KVDatabase {
    void start();
    void stop();
    void handleCommand(Connection connection);
    void registerCommandHandler(Class<?> clazz, CommandHandler handler);
    void unregisterCommandHandler(Class<?> clazz);
}
