package election.statemachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultStateMachine implements StateMachine {
    private static final Logger logger = LoggerFactory.getLogger(DefaultStateMachine.class);
    @Override
    public boolean apply() {
        logger.debug("DefaultMachine apply method was called!");
    }
}
