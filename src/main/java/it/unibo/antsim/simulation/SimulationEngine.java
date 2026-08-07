package it.unibo.antsim.simulation;

import it.unibo.antsim.agent.*;
import it.unibo.antsim.pheromone.PheromoneField;
import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.World;
import it.unibo.antsim.world.WorldPosition;
import it.unibo.antsim.world.generation.GenerationParameters;
import it.unibo.antsim.world.generation.WorldGenerator;
import javafx.scene.control.Cell;

import java.util.*;

/**
 * This class is the skeleton implementation of the core simulationn lifecyle
 */
public class SimulationEngine {
    private SimulationStatus status;
    private final SimulationClock clock;
    private int foodCollected;
    private World world;
    private final WorldGenerator worldGenerator;
    private final GenerationParameters generationParameters;
    private final PheromoneField pheromoneField;
    private final DecisionEngine decisionEngine;
    private final AntGroup antGroup;
    private final AntFactory antFactory;
    private static final double PHEROMONE_FOOD_DEPOSIT_RATE = 3.0;
    private static final double PHEROMONE_HOME_DEPOSIT_RATE = 3.0;

    public SimulationEngine(World world, PheromoneField pheromoneField, DecisionEngine decisionEngine, AntFactory antFactory, WorldGenerator worldGenerator, GenerationParameters generationParameters){
        this.clock = new SimulationClock();
        this.status = SimulationStatus.IDLE;
        this.foodCollected = 0;
        this.world = Objects.requireNonNull(world);
        this.worldGenerator = Objects.requireNonNull(worldGenerator);
        this.generationParameters = Objects.requireNonNull(generationParameters);
        this.pheromoneField = Objects.requireNonNull(pheromoneField);
        this.decisionEngine = Objects.requireNonNull(decisionEngine);
        this.antGroup = new AntGroup();
        this.antFactory = Objects.requireNonNull(antFactory);
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

    public void reset(){
        createEmptyScenario();
    }

    public void generateScenario(){
        status = SimulationStatus.IDLE;
        clock.reset();
        foodCollected = 0;

        antGroup.clear();
        pheromoneField.clear();
        world = worldGenerator.generate(generationParameters);
    }

    public void createEmptyScenario(){
        status = SimulationStatus.IDLE;
        clock.reset();
        foodCollected = 0;

        antGroup.clear();
        pheromoneField.clear();
        world = new World(generationParameters.rows(), generationParameters.cols(), generationParameters.cellWidth(), generationParameters.cellHeight());
    }
    public void step(double dt){
        if(status!=SimulationStatus.RUNNING){
            throw new IllegalStateException("Simulation must be running!");
        }

        updateAgents(dt);
        foodPickup(dt);
        nestDelivery();
        updateEnvironment(dt);
        clock.tick(dt);
    }

    private void updateAgents(double dt){
        for(Ant ant : antGroup.getAnts()){
            boolean returningToNest = ant.getState() == AntState.RETURNING_TO_NEST;

            ant.setAngle(decisionEngine.decideNextAngle(ant, world, pheromoneField));
            ant.move(dt, world);

            CellIndex prev = ant.getPrevCell();
            if(prev == null){
                continue;
            }

            if(returningToNest){
                pheromoneField.deposit(prev, PheromoneField.PheromoneType.FOOD, PHEROMONE_FOOD_DEPOSIT_RATE * dt);
            }else{
                pheromoneField.deposit(prev, PheromoneField.PheromoneType.HOME, PHEROMONE_HOME_DEPOSIT_RATE * dt);
            }
        }
    }

    private void updateEnvironment(double dt){
        pheromoneField.evaporate(dt);
    }

    public World getWorld(){
        return world;
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
                antGroup.size(),
                foodCollected
        );
    }

    public void addAnt(Ant ant){
        this.antGroup.addAnt(Objects.requireNonNull(ant));
    }

    public List<Ant> getAnts() {
        return Collections.unmodifiableList(antGroup.getAnts());
    }

    /**
     * Interaction between wandering ants and food source
     * basically ants near food collect and switch their stare to RETURNING_TO_NEST
     */
    public void foodPickup(double dt){
        for(Ant ant : antGroup.getAnts()){
            if(ant.getState()!=AntState.WANDERING){
                continue;
            }

            world.findFoodCellNear(ant.getPosition()).filter(world::consumeFood).ifPresent(ignored->{
                ant.pickFood();
            });
        }
    }

    public void nestDelivery(){
        for(Ant ant : antGroup.getAnts()){
            if(ant.getState()!=AntState.RETURNING_TO_NEST || !world.isNestAt(ant.getPosition())){
                continue;
            }

            foodCollected++;
            ant.dropFood();
        }

    }

    /**
     * This method dynamically adjusts the number of active ants in the simulation
     * and the new ants will be generated ad nest position
     * @param targetCount number of desired ants
     */
    public void setAgentCount(int targetCount){
        if(targetCount<0){
            throw new IllegalArgumentException("Agent count must be a positive value!");
        }

        CellIndex nestIndex = Objects.requireNonNull(world.getNestIndex(), "There must be a nest before creating ants");
        double cellWidth = world.getWidth()/world.getColumns();
        double cellHeight = world.getHeight()/ world.getRows();

        while(antGroup.size()<targetCount){
            antGroup.addAnt(antFactory.generateAntInNest(nestIndex, cellWidth, cellHeight));
        }
    }
}
