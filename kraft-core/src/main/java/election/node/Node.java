package election.node;

public interface Node {
    void start();
    void stop();
    boolean apply(byte[] command);
}
