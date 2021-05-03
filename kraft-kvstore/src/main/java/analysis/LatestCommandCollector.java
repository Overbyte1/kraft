package analysis;

public class LatestCommandCollector implements Collector {
    @Override
    public int getType() {
        return AnalysisType.LATEST_COMMAND;
    }

    @Override
    public String collect() {
        return "latest command";
    }
}
