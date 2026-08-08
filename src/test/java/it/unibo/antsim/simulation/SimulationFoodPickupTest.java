package it.unibo.antsim.simulation;

import it.unibo.antsim.agent.AcoDecisionEngine;
import it.unibo.antsim.agent.AcoParameters;
import it.unibo.antsim.agent.Ant;
import it.unibo.antsim.agent.AntFactory;
import it.unibo.antsim.agent.AntRole;
import it.unibo.antsim.pheromone.Evaporation;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationFoodPickupTest {
    private SimulationEngine engine;
    private World world;

    @BeforeEach
    void setUp() {
        final int r = 5;
        final int c = 5;
        final long seed = 42L;
        world = new World(r, c, 10.0, 10.0);
        final PheromoneMap pheromoneMap = new PheromoneMap(r, c, 10.0, 10.0, 100.0, new Evaporation(0.3));
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
                        1)
        );
    }

    @Test
    void testFoodConsumptionIsProportionalToAnts() {
        final int foodQ = 5;
        final CellIndex foodCellIndex = new CellIndex(2, 2);
        world.getGrid().setCellContent(foodCellIndex, new CellContent.Food(foodQ));

        final Ant one = new Ant(new WorldPosition(25.0, 25.0), 0.0, 0.0, AntRole.FOLLOWER);
        final Ant two = new Ant(new WorldPosition(25.0, 25.0), 0.0, 0.0, AntRole.FOLLOWER);
        final Ant three = new Ant(new WorldPosition(25.0, 25.0), 0.0, 0.0, AntRole.FOLLOWER);

        engine.addAnt(one);
        engine.addAnt(two);
        engine.addAnt(three);
        engine.start();

        engine.step(1.0);

        assertTrue(one.isCarryingFood());
        assertTrue(two.isCarryingFood());
        assertTrue(three.isCarryingFood());

        final CellContent.Food remainingFood = (CellContent.Food) world.getGrid().getCellAt(foodCellIndex).getCellContent();
        assertEquals(2, remainingFood.quantity());
    }
}
