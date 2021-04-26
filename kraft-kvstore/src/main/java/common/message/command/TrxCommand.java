package common.message.command;

import java.util.List;

public class TrxCommand {
    private List<Object> commands;

    public TrxCommand(List<Object> commands) {
        this.commands = commands;
    }

    public List<Object> getCommands() {
        return commands;
    }

    public void setCommands(List<Object> commands) {
        this.commands = commands;
    }
}