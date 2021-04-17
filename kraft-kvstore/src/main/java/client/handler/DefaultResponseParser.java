package client.handler;

import client.CommandContext;
import common.message.response.*;

public class DefaultResponseParser implements ResponseParser {
    @Override
    public String parse(Object resp, CommandHandler commandHandler, String[] args, CommandContext commandContext) {
        Response response = (Response) resp;
        int type = response.getType();
        if(type == ResponseType.FAILURE) {
            return ((FailureResult)(response.getBody())).getErrorMessage();
        }
        if(type == ResponseType.REDIRECT) {
            String ret =  "redirect to " + ((RedirectResult)(response.getBody())).getNodeEndpoint().toString();
            System.out.println(ret);
            commandHandler.execute(args, commandContext);
            return "";
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
