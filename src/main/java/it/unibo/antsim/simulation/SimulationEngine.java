package it.unibo.antsim.simulation;

import it.unibo.antsim.agent.Ant;
import it.unibo.antsim.agent.AntFactory;
import it.unibo.antsim.agent.AntState;
import it.unibo.antsim.agent.DecisionEngine;
import it.unibo.antsim.pheromone.PheromoneField;
import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.World;
import it.unibo.antsim.world.WorldPosition;
import it.unibo.antsim.world.generation.GenerationParameters;
import it.unibo.antsim.world.generation.WorldGenerator;

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
    private final List<Ant> ants;
    private final AntFactory antFactory;
    private static final double PHEROMONE_DEPOSIT_CONSTANT = 100.0;
    private static final double MIN_TRIP_LENGTH = 1.0;
    private final Map<Ant, Double> returnTripLength;
    private final Map<Ant, List<CellIndex>> returnPath;

    public SimulationEngine(World world, PheromoneField pheromoneField, DecisionEngine decisionEngine, AntFactory antFactory, WorldGenerator worldGenerator, GenerationParameters generationParameters){
        this.clock = new SimulationClock();
        this.status = SimulationStatus.IDLE;
        this.foodCollected = 0;
        this.world = Objects.requireNonNull(world);
        this.worldGenerator = Objects.requireNonNull(worldGenerator);
        this.generationParameters = Objects.requireNonNull(generationParameters);
        this.pheromoneField = Objects.requireNonNull(pheromoneField);
        this.decisionEngine = Objects.requireNonNull(decisionEngine);
        this.ants = new ArrayList<>();
        this.antFactory = Objects.requireNonNull(antFactory);
        this.returnTripLength = new IdentityHashMap<>();
        this.returnPath = new IdentityHashMap<>();
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
        status = SimulationStatus.IDLE;
        clock.reset();
        foodCollected = 0;

        ants.clear();
        returnTripLength.clear();
        returnPath.clear();

        world  =worldGenerator.generate(generationParameters);
        pheromoneField.clear();
    }
    public void step(double dt){
        if(status!=SimulationStatus.RUNNING){
            throw new IllegalStateException("Simulation must be running!");
        }

        updateAgents(dt);
        foodPickup();
        nestDelivery();
        updateEnvironment(dt);
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
                List<CellIndex> path = returnPath.get(ant);
                if(path==null){
                    path = new ArrayList<>();
                    returnPath.put(ant, path);
                }
                returnTripLength.merge(ant, distanceMoved, Double::sum);
                CellIndex currentCell = world.convertToCellIndex(currentPos);
                if(path.isEmpty() || !path.get(path.size()-1).equals(currentCell)){
                    path.add(currentCell);
                }
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
                returnPath.put(ant, new ArrayList<>());
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
            List<CellIndex> path = returnPath.get(ant);
            if(path!=null && !path.isEmpty()){
                double depositPerCell  =depositAmount/path.size();
                for(CellIndex cell:path){
                    pheromoneField.deposit(cell, PheromoneField.PheromoneType.FOOD, depositPerCell);
                }
            }
            foodCollected++;
            ant.dropFood();
            returnTripLength.remove(ant);
            returnPath.remove(ant);
            System.out.println("NEST_DELIVERY ant=" + ant.getPosition() + " state=" + ant.getState() + " trip=" + returnTripLength.get(ant));
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

        while(ants.size()<targetCount){
            ants.add(antFactory.generateAntInNest(nestIndex, cellWidth, cellHeight));
        }
        while(ants.size()>targetCount){
            Ant removedAnt = ants.remove(ants.size() - 1);
            returnTripLength.remove(removedAnt);
            returnPath.remove(removedAnt);
        }
    }
}
