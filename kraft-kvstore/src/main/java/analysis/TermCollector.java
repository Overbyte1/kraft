package analysis;

import election.node.Node;

public class TermCollector implements Collector {
    private final Node node;

    public TermCollector(Node node) {
        this.node = node;
    }

    @Override
    public int getType() {
        return AnalysisType.GET_TERM;
    }

    @Override
    public String collect() {
        return String.valueOf(node.getCurrentTerm());
    }
}
