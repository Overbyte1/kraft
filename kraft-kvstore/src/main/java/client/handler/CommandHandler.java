package client.handler;

public interface CommandHandler {
    String getCommandName();
    void execute(String[] args);
}
