package server;

import common.message.Connection;
import server.handler.CommandHandler;

public interface KVDatabase {
    void start();
    void stop();
    void handleCommand(Connection connection);
    void registerCommandHandler(Class<?> clazz, CommandHandler handler);
    void unregisterCommandHandler(Class<?> clazz);
    void addBeforeListener(KVListener listener);
    void addAfterListener(KVListener listener);
}
