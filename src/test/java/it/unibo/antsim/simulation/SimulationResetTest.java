package it.unibo.antsim.simulation;

import it.unibo.antsim.agent.AntFactory;
import it.unibo.antsim.agent.DecisionEngine;
import it.unibo.antsim.pheromone.Evaporation;
import it.unibo.antsim.pheromone.PheromoneField;
import it.unibo.antsim.pheromone.PheromoneMap;
import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.World;
import it.unibo.antsim.world.WorldPosition;
import it.unibo.antsim.world.generation.GenerationParameters;
import it.unibo.antsim.world.generation.WorldGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class SimulationResetTest {
    private SimulationEngine engine;
    private PheromoneMap pheromoneMap;

    @BeforeEach
    void setUp() {
        final GenerationParameters params = new GenerationParameters(
                10,
                10,
                10.0,
                10.0,
                0.0,
                0,
                0,
                10,
                1
        );
        final WorldGenerator worldGen = new WorldGenerator(new Random(42));
        final World world = worldGen.generate(params);
        pheromoneMap = new PheromoneMap(10, 10, 10.0, 10.0, 100.0, new Evaporation(0.0));
        final DecisionEngine keepDirection = (ant, testWorld, pheromoneField) -> ant.getAngle();
        final long seed = 42L;
        engine = new SimulationEngine(world,
                pheromoneMap,
                keepDirection,
                new AntFactory(1.0, new Random(seed)),
                worldGen,
                params
        );
    }

    @Test
    void resetRegeneratesWorldAndClearsSimulationState() {
        final World prevWorld = engine.getWorld();
        final CellIndex nest = prevWorld.getNestIndex();
        final double intensity = 50.0;
        final int agentCount = 5;
        final double x = 55.0;
        final double y = 55.0;
        pheromoneMap.deposit(nest, PheromoneField.PheromoneType.FOOD, intensity);

        engine.setAgentCount(agentCount);
        engine.start();
        engine.step(1.0);

        engine.reset();

        assertEquals(SimulationStatus.IDLE, engine.getStatus());
        assertEquals(0, engine.getClock().getCurrentStep());
        assertEquals(0.0, engine.getClock().getTotalTime());
        assertEquals(0, engine.getStats().foodCollected());
        assertEquals(0, engine.getAnts().size());

        assertNotSame(prevWorld, engine.getWorld());

        assertEquals(0.0, pheromoneMap.level(new WorldPosition(x, y), PheromoneField.PheromoneType.FOOD));
    }
}
