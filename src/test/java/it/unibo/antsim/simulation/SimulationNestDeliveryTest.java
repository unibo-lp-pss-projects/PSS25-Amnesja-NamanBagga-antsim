package it.unibo.antsim.simulation;

import it.unibo.antsim.agent.AcoParameters;
import it.unibo.antsim.agent.AcoDecisionEngine;
import it.unibo.antsim.agent.AntFactory;
import it.unibo.antsim.agent.AntRole;
import it.unibo.antsim.agent.AntState;
import it.unibo.antsim.agent.Ant;
import it.unibo.antsim.agent.DecisionEngine;
import it.unibo.antsim.pheromone.Evaporation;
import it.unibo.antsim.pheromone.PheromoneField;
import it.unibo.antsim.pheromone.PheromoneMap;
import it.unibo.antsim.world.CellContent;
import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.World;
import it.unibo.antsim.world.WorldPosition;
import it.unibo.antsim.world.generation.GenerationParameters;
import it.unibo.antsim.world.generation.WorldGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SimulationNestDeliveryTest {
    private World world;
    private SimulationEngine engine;

    @BeforeEach
    void setUp() {
        final long seed = 42L;
        world = new World(10, 10, 10.0, 10.0);
        final PheromoneMap pheromoneMap = new PheromoneMap(10, 10, 10.0, 10.0, 500.0, new Evaporation(0.3));
        final AcoParameters param = new AcoParameters(1.0, 1.0, 5.0, Math.PI / 4, 0.0, 1.0);
        engine = new SimulationEngine(
                world,
                pheromoneMap,
                new AcoDecisionEngine(param, new Random(seed)),
                new AntFactory(1.0, new Random(seed)),
                new WorldGenerator(new Random(seed)),
                new GenerationParameters(
                        10,
                        10,
                        10.0,
                        10.0,
                        0.0,
                        0,
                        0,
                        10,
                        1
                )
        );
    }

    @Test
    void testNestDeliveryResetsAntStateAndIncrementFood() {
        final int r = 5;
        final int c = 5;
        final double dt = 0.1;
        world.relocateNest(new CellIndex(r, c));
        final Ant ant = new Ant(new WorldPosition(55.0, 55.0), 0.0, 0.0, AntRole.FOLLOWER);
        ant.pickFood();

        engine.addAnt(ant);
        engine.start();
        engine.step(dt);

        assertEquals(AntState.WANDERING, ant.getState());
        assertFalse(ant.isCarryingFood());
        assertEquals(1, engine.getStats().foodCollected());
    }

    @Test
    void testReturningAntDepositsPheromoneAlongItsPath() {
        final PheromoneMap shortTripPheromones = simulateDelivery(new CellIndex(5, 3), new WorldPosition(35.0, 55.0), 2);
        final PheromoneMap longTripPheromones = simulateDelivery(new CellIndex(5, 1), new WorldPosition(15.0, 55.0), 4);

        final double expected1 = 6.0;
        final double expected2 = 12.0;
        assertEquals(expected1, totalFoodPheromone(shortTripPheromones));
        assertEquals(expected2, totalFoodPheromone(longTripPheromones));
    }

    private PheromoneMap simulateDelivery(final CellIndex food, final WorldPosition initialPos, final int steps) {
        final int r = 5;
        final int c = 5;
        world = new World(10, 10, 10.0, 10.0);
        world.relocateNest(new CellIndex(r, c));

        world.getGrid().setCellContent(food, new CellContent.Food(1));

        final PheromoneMap pheromoneMap = new PheromoneMap(10, 10, 10.0, 10.0, 500.0, new Evaporation(0.0));

        // 0 rad to simulate the movement pointing to the nest direction
        final DecisionEngine moveRight = (ant, testWorld, field) -> 0.0;

        final SimulationEngine testEngine = new SimulationEngine(
                world,
                pheromoneMap,
                moveRight,
                new AntFactory(1.0, new Random(42)),
                new WorldGenerator(new Random(42)),
                new GenerationParameters(
                        10,
                        10,
                        10.0,
                        10.0,
                        0.0,
                        0,
                        0,
                        10,
                        1
                )
        );

        final Ant ant = new Ant(initialPos, 0.0, 10.0, AntRole.FOLLOWER);
        ant.pickFood();
        testEngine.addAnt(ant);
        testEngine.start();

        for (int i = 0; i < steps; i++) {
            testEngine.step(1.0);
        }
        return pheromoneMap;
    }

    private double totalFoodPheromone(final PheromoneMap pheromoneMap) {
        double total = 0.0;
        final double value = 5.0;
        for (int row = 0; row < 10; row++) {
            for (int column = 0; column < 10; column++) {
                total += pheromoneMap.level(
                        new WorldPosition(column * 10.0 + value, row * 10.0 + value),
                        PheromoneField.PheromoneType.FOOD
                );
            }
        }
        return total;
    }
}
