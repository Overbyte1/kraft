package election.statemachine;

public interface StateMachine {
    boolean apply(int[] command);
}
