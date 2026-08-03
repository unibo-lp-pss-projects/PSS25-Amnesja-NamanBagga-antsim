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
    private final Map<Ant, Double> returnTripLength;
    private final Map<Ant, List<CellIndex>> returnPath;
    private final Map<Ant, List<CellIndex>> outboundPath;
    private final Map<Ant, Integer> returnCursor;

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
        this.returnTripLength = new IdentityHashMap<>();
        this.returnPath = new IdentityHashMap<>();
        this.outboundPath = new IdentityHashMap<>();
        this.returnCursor = new IdentityHashMap<>();
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

        antGroup.clear();
        returnTripLength.clear();
        returnPath.clear();
        outboundPath.clear();
        returnCursor.clear();

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
        for(Ant ant : antGroup.getAnts()){
            WorldPosition prevPos = ant.getPosition();
            boolean returningToNest = ant.getState() == AntState.RETURNING_TO_NEST;

            if(returningToNest){
                ant.setAngle(returnAngle(ant, dt));
            }else{
                ant.setAngle(decisionEngine.decideNextAngle(ant, world, pheromoneField));
            }

            ant.move(dt, world);

            CellIndex current = world.convertToCellIndex(ant.getPosition());

            if(returningToNest){
                pheromoneField.deposit(current, PheromoneField.PheromoneType.FOOD, PHEROMONE_FOOD_DEPOSIT_RATE * dt);

                double movedDist = Math.hypot(ant.getPosition().x() - prevPos.x(), ant.getPosition().y() - prevPos.y());

                returnTripLength.merge(ant, movedDist, Double::sum);


                List<CellIndex> path = returnPath.computeIfAbsent(ant, ignored -> new ArrayList<>());

                if(path.isEmpty() || !path.get(path.size() - 1).equals(current)){
                    path.add(current);
                }
            }else{
                recordOutboundCell(ant, current);
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
    public void foodPickup(){
        for(Ant ant : antGroup.getAnts()){
            if(ant.getState()!=AntState.WANDERING){
                continue;
            }

            world.findFoodCellNear(ant.getPosition()).filter(world::consumeFood).ifPresent(ignored->{
                recordOutboundCell(ant, world.convertToCellIndex(ant.getPosition()));
                ant.pickFood();
                returnTripLength.put(ant, 0.0);
                returnPath.put(ant, new ArrayList<>());
                List<CellIndex> path = outboundPath.get(ant);
                returnCursor.put(ant, path.size() - 1);
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
            returnTripLength.remove(ant);
            returnPath.remove(ant);
            outboundPath.remove(ant);
            returnCursor.remove(ant);
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
        while(antGroup.size()>targetCount){
            Ant removedAnt = antGroup.removeLast();
            returnTripLength.remove(removedAnt);
            returnPath.remove(removedAnt);
        }
    }

    private List<CellIndex> getCellsBetween(CellIndex from, CellIndex to){
        List<CellIndex> cells = new ArrayList<>();
        int r0 = from.row();
        int c0 = from.column();
        int r1 = to.row();
        int c1 = to.column();

        int dr =Math.abs(r1 - r0);
        int dc = Math.abs(c1 - c0);
        int sr = r0 < r1 ? 1 : -1;
        int sc = c0 < c1 ? 1 : -1;
        int err = dr - dc;
        int r = r0;
        int c = c0;

        while(true){
            cells.add(new CellIndex(r, c));
            if(r == r1 && c == c1){
                break;
            }
            int e2 = 2 * err;
            if(e2 > -dc) {
                err -= dc;
                r += sr;
            }
            if(e2 < dr){
                err += dr;
                c += sc;
            }
        }
        return cells;
    }

    private void recordOutboundCell(Ant ant, CellIndex cell){
        List<CellIndex> path = outboundPath.computeIfAbsent(ant, ignored -> new ArrayList<>());

        int prevOccurence = path.lastIndexOf(cell);

        if(prevOccurence >= 0){
            path.subList(prevOccurence +1, path.size()).clear();
        }else{
            path.add(cell);
        }
    }

    private double returnAngle(Ant ant, double dt){
        List<CellIndex> path = outboundPath.get(ant);

        if(path == null || path.isEmpty()){
            return ant.getAngle();
        }

        int cursor = returnCursor.getOrDefault(ant, path.size() - 1);

        double cellWidth = world.getWidth() / world.getColumns();
        double cellHeight = world.getHeight() / world.getRows();
        double reachDistance = ant.getSpeed() * dt * 1.1;
        while(cursor >= 0){
            CellIndex target = path.get(cursor);

            WorldPosition targetPos = new WorldPosition((target.column() + 0.5) * cellWidth, (target.row() + 0.5) * cellHeight);
            double distance = Math.hypot(targetPos.x() - ant.getPosition().x(), targetPos.y() - ant.getPosition().y());

            if(distance > reachDistance){
                returnCursor.put(ant, cursor);

                return Math.atan2(targetPos.y() - ant.getPosition().y(), targetPos.x() - ant.getPosition().x());
            }

            cursor--;
        }

        returnCursor.put(ant, cursor);
        return ant.getAngle();
    }
}
