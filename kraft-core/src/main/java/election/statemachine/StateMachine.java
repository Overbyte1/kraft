package election.statemachine;

public interface StateMachine {
    boolean apply(byte[] command);
}
