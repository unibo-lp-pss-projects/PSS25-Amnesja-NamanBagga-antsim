package it.unibo.antsim.simulation;

import it.unibo.antsim.agent.AcoDecisionEngine;
import it.unibo.antsim.agent.AcoParameters;
import it.unibo.antsim.agent.AntFactory;
import it.unibo.antsim.pheromone.Evaporation;
import it.unibo.antsim.pheromone.PheromoneMap;
import it.unibo.antsim.world.World;
import it.unibo.antsim.world.generation.GenerationParameters;
import it.unibo.antsim.world.generation.WorldGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimulationEngineLifecycleTest {
    private SimulationEngine engine;

    @BeforeEach
    void setUp() {
        final long seed = 42L;
        final double decayRate = 0.3;
        final double sensorRange = 5.0;

        engine = new SimulationEngine(
                new World(10, 10, 10.0, 10.0),
                new PheromoneMap(10, 10, 10.0, 10.0, 100.0, new Evaporation(decayRate)),
                new AcoDecisionEngine(
                        new AcoParameters(1.0, 1.0, sensorRange, Math.PI / 4, 0.0, 1.0),
                        new Random(seed)
                ),
                new AntFactory(1.0, new Random(seed)),
                new WorldGenerator(new Random(seed)),
                new GenerationParameters(10,
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
    void testInitialStateIsIdle() {
        assertEquals(SimulationStatus.IDLE, engine.getStatus());
    }

    @Test
    void testValidLifecycleTransitions() {
        engine.start();
        assertEquals(SimulationStatus.RUNNING, engine.getStatus());

        engine.pause();
        assertEquals(SimulationStatus.PAUSED, engine.getStatus());

        engine.resume();
        assertEquals(SimulationStatus.RUNNING, engine.getStatus());

        engine.stop();
        assertEquals(SimulationStatus.STOPPED, engine.getStatus());
    }

    @Test
    void testInvalidTransitionThrowException() {
        // Cannot pause while IDLE
        assertThrows(IllegalStateException.class, engine::pause);

        // Cannot resume while IDLE
        assertThrows(IllegalStateException.class, engine::resume);

        engine.start();
        // Cannot start while already running
        assertThrows(IllegalStateException.class, engine::start);
    }
}
