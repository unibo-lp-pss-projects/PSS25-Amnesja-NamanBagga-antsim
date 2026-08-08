package it.unibo.antsim.simulation;

/**
 * This class tracks physical time progression and tick steps in simulation.
 */
public class SimulationClock {
    private long currentStep;
    private double totalTime;

    /**
     * Instantiates a new Simulation clock.
     */
    public SimulationClock() {
        this.currentStep = 0;
        this.totalTime = 0.0;
    }

    /**
     * Advances the clock by 1 time step.
     *
     * @param dt time delta for step
     */
    public void tick(final double dt) {
        if (dt < 0) {
            throw new IllegalArgumentException("Time delta (dt) must be positive!");
        }
        this.currentStep++;
        this.totalTime += dt;
    }

    /**
     * Get current step long.
     *
     * @return the current step
     */
    public long getCurrentStep() {
        return currentStep;
    }

    /**
     * Get total time double.
     *
     * @return the total time
     */
    public double getTotalTime() {
        return totalTime;
    }

    /**
     * Reset method.
     */
    public void reset() {
        this.currentStep = 0;
        this.totalTime = 0.0;
    }
}
