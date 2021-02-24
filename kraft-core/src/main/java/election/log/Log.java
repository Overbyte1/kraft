package election.log;

public interface Log {
    //TODO:提供那些方法
    void advanceCommit();
    boolean isNewerThan(long logIndex);


}
