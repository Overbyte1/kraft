package election.log.store;

public class FileFormatUnSupportException extends RuntimeException {
    public FileFormatUnSupportException(String msg) {
        super(msg);
    }
    public FileFormatUnSupportException(){}
}
