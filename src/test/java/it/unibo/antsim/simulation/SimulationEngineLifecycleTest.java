package it.unibo.antsim.simulation;

import it.unibo.antsim.agent.AcoDecisionEngine;
import it.unibo.antsim.agent.AcoParameters;
import it.unibo.antsim.pheromone.Evaporation;
import it.unibo.antsim.pheromone.PheromoneMap;
import it.unibo.antsim.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SimulationEngineLifecycleTest {
    private SimulationEngine engine;

    @BeforeEach
    void setUp(){
        engine = new SimulationEngine(
                new World(10, 10, 10.0, 10.0),
                new PheromoneMap(10, 10, 10.0, 10.0, 100.0, new Evaporation(0.3)),
                new AcoDecisionEngine(
                        new AcoParameters(1.0, 1.0, 5.0, Math.PI/4, 0.0, 1.0),
                        new Random(42)
                ));
    }

    @Test
    void testInitialStateIsIdle(){
        assertEquals(SimulationStatus.IDLE, engine.getStatus());
    }

    @Test
    void testValidLifecycleTrainsitions(){
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
    void testInvalidTrainsitionThrowException(){
        // Cannot pause while IDLE
        assertThrows(IllegalStateException.class, engine::pause);

        // Cannot resume while IDLE
        assertThrows(IllegalStateException.class, engine::resume);

        engine.start();
        // Cannot start while already running
        assertThrows(IllegalStateException.class, engine::start);
    }
}
