package election.statemachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultStateMachine implements StateMachine {
    private static final Logger logger = LoggerFactory.getLogger(DefaultStateMachine.class);
    @Override
    public boolean apply(int[] command) {
        logger.debug("DefaultMachine appendLog method was called!");
        return false;
    }
}
