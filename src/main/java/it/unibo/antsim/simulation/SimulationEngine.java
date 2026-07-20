package it.unibo.antsim.simulation;

/**
 * This class is the skeleton implementation of the core simulationn lifecyle
 */
public class SimulationEngine {
    private SimulationStatus status;
    private final SimulationClock clock;
    private int foodCollected;

    public SimulationEngine(){
        this.clock = new SimulationClock();
        this.status = SimulationStatus.IDLE;
        this.foodCollected = 0;
    }

    public void start(){
        if(status==SimulationStatus.RUNNING){
            throw new IllegalStateException("Simulation already running!");
        }
        this.status = SimulationStatus.RUNNING;
    }

    public void pause(){
        if(status!=SimulationStatus.RUNNING){
            throw new IllegalStateException("Cannot pause a not running simulation!");
        }
        this.status = SimulationStatus.PAUSED;
    }

    public void resume(){
        if(status!=SimulationStatus.PAUSED){
            throw new IllegalStateException("Cannot resume a not paused simulation!");
        }
        this.status = SimulationStatus.RUNNING;
    }

    public void stop(){
        this.status = SimulationStatus.STOPPED;
    }

    public SimulationStatus getStatus(){
        return status;
    }

    public SimulationClock getClock(){
        return clock;
    }

    public SimulationStatistics getStats(int activeAnts){
        return new SimulationStatistics(
                clock.getCurrentStep(),
                clock.getTotalTime(),
                activeAnts,
                foodCollected
        );
    }
}
