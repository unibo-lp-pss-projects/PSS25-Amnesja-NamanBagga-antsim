package it.unibo.antsim.simulation;

import it.unibo.antsim.agent.Ant;
import it.unibo.antsim.agent.AntState;
import it.unibo.antsim.agent.DecisionEngine;
import it.unibo.antsim.pheromone.PheromoneField;
import it.unibo.antsim.world.World;
import it.unibo.antsim.world.WorldPosition;

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
    private static final double PHEROMONE_DEPOSIT_CONSTANT = 100.0;
    private static final double MIN_TRIP_LENGTH = 1.0;
    private final Map<Ant, Double> returnTripLength;

    public SimulationEngine(World world, PheromoneField pheromoneField, DecisionEngine decisionEngine){
        this.clock = new SimulationClock();
        this.status = SimulationStatus.IDLE;
        this.foodCollected = 0;
        this.world = Objects.requireNonNull(world);
        this.pheromoneField = Objects.requireNonNull(pheromoneField);
        this.decisionEngine = Objects.requireNonNull(decisionEngine);
        this.ants = new ArrayList<>();
        this.returnTripLength = new IdentityHashMap<>();
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

        // Food pickup
        foodPickup();

        // Food delivery to nest
        nestDelivery();

        // Pheromone evaporation
        updateEnvironment(dt);

        // Advance clock
        clock.tick(dt);
    }

    private void updateAgents(double dt){
        for(Ant ant : ants){
            WorldPosition prevPos = ant.getPosition();
            boolean returningToNest = ant.getState() == AntState.RETURNING_TO_NEST;

            double nextAngle = decisionEngine.decideNextAngle(ant, world, pheromoneField);
            ant.setAngle(nextAngle);
            ant.move(dt, world);

            if(returningToNest){
                WorldPosition currentPos = ant.getPosition();
                double distanceMoved = Math.hypot(
                        currentPos.x() - prevPos.x(),
                        currentPos.y() - prevPos.y()
                );

                returnTripLength.merge(ant, distanceMoved, Double::sum);
            }
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

    public SimulationStatistics getStats(){
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

    /**
     * Interaction between wandering ants and food source
     * basically ants near food collect and switch their stare to RETURNING_TO_NEST
     */
    public void foodPickup(){
        for(Ant ant : ants){
            if(ant.getState()!=AntState.WANDERING){
                continue;
            }

            world.findFoodCellNear(ant.getPosition()).filter(world::consumeFood).ifPresent(ignored->{
                ant.pickFood();
                returnTripLength.put(ant, 0.0);
            });
        }
    }

    public void nestDelivery(){
        for(Ant ant : ants){
            if(ant.getState()!=AntState.RETURNING_TO_NEST || !world.isNestAt(ant.getPosition())){
                continue;
            }

            double tripLength = Math.max(MIN_TRIP_LENGTH, returnTripLength.getOrDefault(ant, 0.0));
            double depositAmount = PHEROMONE_DEPOSIT_CONSTANT/tripLength;

            pheromoneField.deposit(world.getNestIndex(), PheromoneField.PheromoneType.FOOD, depositAmount);

            foodCollected++;
            ant.dropFood();
            returnTripLength.remove(ant);
        }
    }

}
