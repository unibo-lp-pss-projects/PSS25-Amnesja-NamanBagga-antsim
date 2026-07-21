package it.unibo.antsim.simulation;

import it.unibo.antsim.agent.Ant;
import it.unibo.antsim.agent.DecisionEngine;
import it.unibo.antsim.pheromone.PheromoneField;
import it.unibo.antsim.world.World;

import java.util.*;

/**
 * This class is the skeleton implementation of the core simulationn lifecyle
 */
public class SimulationEngine {
    private SimulationStatus status;
    private final SimulationClock clock;
    private int foodCollected;
    private final World world;
    private final PheromoneField pheromoneField;
    private final DecisionEngine decisionEngine;
    private final List<Ant> ants;

    public SimulationEngine(World world, PheromoneField pheromoneField, DecisionEngine decisionEngine){
        this.clock = new SimulationClock();
        this.status = SimulationStatus.IDLE;
        this.foodCollected = 0;
        this.world = Objects.requireNonNull(world);
        this.pheromoneField = Objects.requireNonNull(pheromoneField);
        this.decisionEngine = Objects.requireNonNull(decisionEngine);
        this.ants = new ArrayList<>();
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

    public void step(double dt){
        if(status!=SimulationStatus.RUNNING){
            throw new IllegalStateException("Simulation must be running!");
        }

        // Agent decision and physical movements
        updateAgents(dt);

        // Pheromone evaporation
        updateEnvironment(dt);

        // Advance clock
        clock.tick(dt);
    }

    private void updateAgents(double dt){
        for(Ant ant : ants){
            // Decision
            double nextAngle = decisionEngine.decideNextAngle(ant, world, pheromoneField);
            ant.setAngle(nextAngle);

            ant.move(dt, world);
        }
    }

    private void updateEnvironment(double dt){
        pheromoneField.evaporate(dt);
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
                ants.size(),
                foodCollected
        );
    }

    public void addAnt(Ant ant){
        this.ants.add(Objects.requireNonNull(ant));
    }

    public List<Ant> getAnts() {
        return Collections.unmodifiableList(ants);
    }



}
