package common.message.response;

public enum FailureResult {
    TIMEOUT(100, "执行超时"),
    NO_LEADER(101, "Leader节点尚未选举出"),
    NOT_SUPPORT_OPERATION(102, "不支持该操作"),
    SERVER_INTERVAL_ERROR(103, "服务器内部错误"),
    TRX_FAIL(104, "事务执行失败");

    private int code;
    private String errorMessage;

    private FailureResult(int code, String errorMessage) {
        this.code = code;
        this.errorMessage = errorMessage;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
