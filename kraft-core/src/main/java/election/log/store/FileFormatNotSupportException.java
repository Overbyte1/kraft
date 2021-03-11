package election.log.store;

public class FileFormatNotSupportException extends RuntimeException {
    public FileFormatNotSupportException(String msg) {
        super(msg);
    }
    public FileFormatNotSupportException(){}
}
