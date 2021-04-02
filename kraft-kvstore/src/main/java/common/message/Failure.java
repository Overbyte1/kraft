package common.message;

public enum  Failure {
    TIMEOUT(100, "执行超时"),
    NO_LEADER(101, "Leader节点尚未选举出"),
    NOT_SUPPORT_OPERATION(102, "不支持该操作");

    private int code;
    private String errorMessage;

    private Failure(int code, String errorMessage) {
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
