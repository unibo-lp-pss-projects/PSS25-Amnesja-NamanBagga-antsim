package it.unibo.antsim.simulation;

import it.unibo.antsim.agent.*;
import it.unibo.antsim.pheromone.Evaporation;
import it.unibo.antsim.pheromone.PheromoneField;
import it.unibo.antsim.pheromone.PheromoneMap;
import it.unibo.antsim.world.CellContent;
import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.World;
import it.unibo.antsim.world.WorldPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class SimulationNestDeliveryTest {
    private World world;
    private PheromoneMap pheromoneMap;
    private SimulationEngine engine;

    @BeforeEach
    void setUp(){
        world = new World(10, 10, 10.0, 10.0);
        PheromoneMap pheromoneMap = new PheromoneMap(10, 10, 10.0, 10.0, 500.0, new Evaporation(0.3));
        AcoParameters param = new AcoParameters(1.0, 1.0, 5.0, Math.PI/4, 0.0, 1.0);
        engine = new SimulationEngine(world, pheromoneMap, new AcoDecisionEngine(param, new Random(42)));
    }

    @Test
    void testNestDeliveryResetsAntstateAndIncrementFood(){
        world.relocateNest(new CellIndex(5, 5));
        Ant ant = new Ant(new WorldPosition(55.0, 55.0), 0.0, 0.0);
        ant.pickFood();

        engine.addAnt(ant);
        engine.start();
        engine.step(0.1);

        assertEquals(AntState.WANDERING, ant.getState());
        assertFalse(ant.isCarryingFood());
        assertEquals(1, engine.getStats().foodCollected());
    }

    @Test
    void testShorterTripdepositMorePheromone(){
        // Shorter trip
        double shortTripPheromone = simulateDelivery(new CellIndex(5, 3), new WorldPosition(35.0, 55.0), 2);
        double longTripPheromone = simulateDelivery(new CellIndex(5, 1), new WorldPosition(15.0, 55.0), 4);

        assertTrue(shortTripPheromone>longTripPheromone,"shorter trip should deposit more pheromone!");
    }

    private double simulateDelivery(CellIndex food, WorldPosition initalPos, int steps){
        World world = new World(10, 10, 10.0, 10.0);
        world.relocateNest(new CellIndex(5, 5));

        world.getGrid().setCellContent(food, new CellContent.Food(1));

        PheromoneMap pheromoneMap = new PheromoneMap(10, 10, 10.0, 10.0, 500.0, new Evaporation(0.0));

        // 0 rad to simulate the movement pointing to the nest direction
        DecisionEngine moveRight = (ant, testWorld, field) -> 0.0;

        SimulationEngine engine = new SimulationEngine(world, pheromoneMap, moveRight);

        Ant ant = new Ant(initalPos, 0.0, 10.0);
        engine.addAnt(ant);
        engine.start();

        for(int i=0; i<steps; i++){
            engine.step(1.0);
        }

        return pheromoneMap.level(new WorldPosition(55.0, 55.0), PheromoneField.PheromoneType.FOOD);
    }
}
