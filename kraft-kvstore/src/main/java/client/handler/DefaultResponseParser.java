package client.handler;

import client.CommandContext;
import client.SendTimeoutException;
import client.SocketChannel;
import client.SocketChannelImpl;
import common.message.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultResponseParser implements ResponseParser {
    private static final Logger logger = LoggerFactory.getLogger(DefaultResponseParser.class);

    private SocketChannel socketChannel = new SocketChannelImpl();

    public DefaultResponseParser(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public DefaultResponseParser() {
    }

    @Override
    public String parse(Object resp, CommandHandler commandHandler, String[] args, CommandContext commandContext) {
        Response response = (Response) resp;
        int type = response.getType();
        if(type == ResponseType.FAILURE) {
            return ((FailureResult)(response.getBody())).getErrorMessage();
        }
        try {
            if(type == ResponseType.REDIRECT) {
                String ret =  "redirect to " + ((RedirectResult)(response.getBody())).getNodeEndpoint().toString();
                System.out.println(ret);
                //commandHandler.execute(args, commandContext);
                RedirectResult redirectResult = (RedirectResult)((Response) resp).getBody();
                //递归解析，注意递归出口
                return parse(socketChannel.send(redirectResult.getNodeEndpoint().getEndpoint(),
                        ((InlineCommandHandler)commandHandler).getSendMessage(args, commandContext)), commandHandler, args, commandContext);
            }
        } catch (SendTimeoutException e) {
            return "timeout";
        } catch (Exception e) {
            logger.warn("exception type: {}, message: {}",e.getClass().getName(), e.getMessage());
            e.printStackTrace();
            return "network exception";
        }
        if(type == ResponseType.SUCCEED) {
            Object body = response.getBody();
            if(body instanceof NoPayloadResult) {
                return "ok";
            }
            if(body instanceof SinglePayloadResult) {
                byte[] bytes = ((SinglePayloadResult)body).getPayload();
                return bytesToString(bytes);
            }
            if(body instanceof MultiPayloadResult) {
                StringBuilder sb = new StringBuilder();
                MultiPayloadResult result = (MultiPayloadResult)body;
                for(byte[] bytes : result.getPayload()) {
                    sb.append(bytesToString(bytes)).append(' ');
                }
                return sb.toString();
            }
        }
        return "unknown error";
    }
    private String bytesToString(byte[] bytes) {
        return bytes == null ? "nil" : new String(bytes);
    }
}
