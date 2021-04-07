package common.message;

import common.message.response.Response;
import io.netty.channel.Channel;

public class Connection<T> {
    private T command;
    private Channel channel;

    public Connection(T command, Channel channel) {
        this.command = command;
        this.channel = channel;
    }

    public T getCommand() {
        return command;
    }
    public void reply(Response response) {
        channel.writeAndFlush(response);
    }
}
