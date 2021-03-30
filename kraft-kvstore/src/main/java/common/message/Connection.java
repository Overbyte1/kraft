package common.message;

import io.netty.channel.socket.nio.NioSocketChannel;

public class Connection<T> {
    private T command;
    private NioSocketChannel channel;

    public Connection(T command, NioSocketChannel channel) {
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
