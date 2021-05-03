package analysis;

public class ThroughoutCollector implements Collector {
    @Override
    public int getType() {
        return AnalysisType.COMMAND_THROUGHOUT;
    }

    @Override
    public String collect() {
        return "throughout";
    }
}
