package common.message.response;

import java.io.Serializable;

public class TrxResult implements Serializable {
    private Response<?>[] responses;

    public TrxResult(Response<?>[] responses) {
        this.responses = responses;
    }

}
