package it.unibo.antsim.simulation;

/**
 * This record is a immutable snapshot of simulation performance and colony stats
 * @param currentStep   total simulation steps elapsed
 * @param timeElapsed   total physical time elapsed in seconds
 * @param activeAnts    number of currently active ants
 * @param foodCollected food collected and brought back at nest
 */
public record SimulationStatistics(
        long currentStep,
        double timeElapsed,
        int activeAnts,
        int foodCollected
) {
    public SimulationStatistics{
        if(currentStep<0 || timeElapsed <0 || activeAnts<0 || foodCollected <0){
            throw new IllegalArgumentException("Stats must be positive!");
        }
    }
}
