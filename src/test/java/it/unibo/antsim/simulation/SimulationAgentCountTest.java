package it.unibo.antsim.simulation;

import it.unibo.antsim.agent.AntFactory;
import it.unibo.antsim.agent.DecisionEngine;
import it.unibo.antsim.pheromone.Evaporation;
import it.unibo.antsim.pheromone.PheromoneMap;
import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.World;
import it.unibo.antsim.world.generation.GenerationParameters;
import it.unibo.antsim.world.generation.WorldGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimulationAgentCountTest {
    private World world;
    private SimulationEngine engine;

    @BeforeEach
    void setUp() {
        world = new World(10, 10, 10.0, 10.0);
        final int r = 5;
        final int c = 5;
        final long seed = 42L;

        world.relocateNest(new CellIndex(r, c));

        final PheromoneMap pheromoneMap = new PheromoneMap(10, 10, 10.0, 10.0, 100.0, new Evaporation(0.0));

        final DecisionEngine keepDirection = (ant, world1, pheromoneField) -> ant.getAngle();
        final AntFactory antFactory = new AntFactory(1.0, new Random(seed));

        engine = new SimulationEngine(world, pheromoneMap, keepDirection, antFactory, new WorldGenerator(new Random(seed)),
                new GenerationParameters(10, 10, 10.0, 10.0, 0.0, 0, 0, 10, 1));
    }

    @Test
    void setAgentCountCreatesAndRemovesAnts() {
        final int agentCount = 5;
        engine.setAgentCount(agentCount);

        assertEquals(agentCount, engine.getAnts().size());
        assertTrue(engine.getAnts().stream().allMatch(ant -> world.isNestAt(ant.getPosition())));

        engine.setAgentCount(2);
        assertEquals(2, engine.getAnts().size());
    }

    @Test
    void setAgentCountRejectsNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> engine.setAgentCount(-1));
    }
}
