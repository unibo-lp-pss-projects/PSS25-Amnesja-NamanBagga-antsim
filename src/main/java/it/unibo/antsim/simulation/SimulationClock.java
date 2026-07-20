package it.unibo.antsim.simulation;

/**
 * This class tracks physical time progression and tick steps in simulation
 */
public class SimulationClock {
    private long currentStep;
    private double totalTime;

    public SimulationClock(){
        reset();
    }

    /**
     * Advances the clock by 1 time step
     * @param dt time delta for step
     */
    public void tick(double dt){
        if(dt<0){
            throw new IllegalArgumentException("Time delta (dt) must be positive!");
        }
        this.currentStep++;
        this.totalTime += dt;
    }

    public long getCurrentStep(){
        return currentStep;
    }

    public double getTotalTime(){
        return totalTime;
    }

    public void reset(){
        this.currentStep = 0;
        this.totalTime = 0.0;
    }
}
