package client.handler;

import client.CommandContext;
import common.message.command.MGetCommand;
import common.message.response.MultiPayloadResult;
import common.message.response.Response;

import java.util.Arrays;

public class MGetHandler extends InlineCommandHandler {
    @Override
    public String getCommandName() {
        return "mget";
    }

    @Override
    public Response<?> doExecute(String[] args, CommandContext commandContext) {
        logger.debug("do mget, keys: {}", Arrays.toString(args));
        return (Response<?>) commandContext.getLoadBalance().send(getSendMessage(args, commandContext));
    }

    @Override
    public Object getSendMessage(String[] args, CommandContext commandContext) {
        return new MGetCommand(args);
    }

    @Override
    public void output(Response<?> msg) {
        StringBuilder sb = new StringBuilder();
        MultiPayloadResult result = (MultiPayloadResult)msg.getBody();
        for(byte[] bytes : result.getPayload()) {
            sb.append(bytesToString(bytes)).append('\n');
        }
        System.out.print(sb.toString());
    }

}
