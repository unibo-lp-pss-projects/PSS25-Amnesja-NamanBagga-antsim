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

import static org.junit.jupiter.api.Assertions.*;

public class SimulationAgentCountTest {
    private World world;
    private SimulationEngine engine;

    @BeforeEach
    void setUp(){
        world = new World(10, 10, 10.0, 10.0);
        world.relocateNest(new CellIndex(5, 5));

        PheromoneMap pheromoneMap = new PheromoneMap(10, 10, 10.0, 10.0, 100.0, new Evaporation(0.0));

        DecisionEngine keepDirection = (ant, world1, pheromoneField) -> ant.getAngle();
        AntFactory antFactory = new AntFactory(1.0, new Random(42));

        engine = new SimulationEngine(world, pheromoneMap, keepDirection, antFactory, new WorldGenerator(new Random(42)),
                new GenerationParameters(10, 10, 10.0, 10.0, 0.0, 0, 0, 10, 1));
    }

    @Test
    void setAgentCountCreatesAndRemovesAnts(){
        engine.setAgentCount(5);

        assertEquals(5, engine.getAnts().size());
        assertTrue(engine.getAnts().stream().allMatch(ant -> world.isNestAt(ant.getPosition())));

        engine.setAgentCount(2);
        assertEquals(2, engine.getAnts().size());
    }

    @Test
    void setAgentCountRejectsNegativeValues(){
        assertThrows(IllegalArgumentException.class, () -> engine.setAgentCount(-1));
    }
}
