package genaicodebase.configuration;

public class SimulationConfig {
    private int maxRounds;

    public SimulationConfig(int maxRounds) {
        this.maxRounds = maxRounds;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

    public void setMaxRounds(int maxRounds) {
        this.maxRounds = maxRounds;
    }

    public boolean validate() {
        return maxRounds > 0;
    }
}
