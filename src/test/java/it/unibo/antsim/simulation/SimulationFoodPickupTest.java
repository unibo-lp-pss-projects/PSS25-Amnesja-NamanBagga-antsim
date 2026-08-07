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

import static org.junit.jupiter.api.Assertions.*;

public class SimulationFoodPickupTest {
    private SimulationEngine engine;
    private World world;

    @BeforeEach
    void setUp() {
        world = new World(5, 5, 10.0, 10.0);
        PheromoneMap pheromoneMap = new PheromoneMap(5, 5, 10.0, 10.0, 100.0, new Evaporation(0.3));
        AcoParameters param = new AcoParameters(1.0, 1.0, 5.0, Math.PI / 4, 0.0, 1.0);
        engine = new SimulationEngine(world, pheromoneMap, new AcoDecisionEngine(param, new Random(42)), new AntFactory(1.0, new Random(42)),new WorldGenerator(new Random(42)),
                new GenerationParameters(10, 10, 10.0, 10.0, 0.0, 0, 0, 10, 1));
    }

    @Test
    void testFoodConsuptionIsProportianalToAnts(){
        CellIndex foodCellIndex = new CellIndex(2, 2);
        world.getGrid().setCellContent(foodCellIndex, new CellContent.Food(5));

        Ant one = new Ant(new WorldPosition(25.0, 25.0), 0.0, 0.0, AntRole.FOLLOWER);
        Ant two = new Ant(new WorldPosition(25.0, 25.0), 0.0, 0.0, AntRole.FOLLOWER);
        Ant three = new Ant(new WorldPosition(25.0, 25.0), 0.0, 0.0, AntRole.FOLLOWER);

        engine.addAnt(one);
        engine.addAnt(two);
        engine.addAnt(three);
        engine.start();

        engine.step(1.0);

        assertTrue(one.isCarryingFood());
        assertTrue(two.isCarryingFood());
        assertTrue(three.isCarryingFood());

        CellContent.Food remainingFood = (CellContent.Food) world.getGrid().getCellAt(foodCellIndex).getCellContent();
        assertEquals(2, remainingFood.quantity());
    }
}
