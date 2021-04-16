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
            commandHandler.execute(args, commandContext);
            return ret;
        }
        if(type == ResponseType.SUCCEED) {
            Object body = response.getBody();
            if(body instanceof NoPayloadResult) {
                return "ok";
            }
            if(body instanceof SinglePayloadResult) {
                return new String(((SinglePayloadResult)body).getPayload());
            }
            if(body instanceof MultiPayloadResult) {
                StringBuilder sb = new StringBuilder();
                MultiPayloadResult result = (MultiPayloadResult)body;
                for(byte[] bytes : result.getPayload()) {
                    sb.append(new String(bytes)).append(' ');
                }
            }
        }
        return "unknown error";
    }
}
