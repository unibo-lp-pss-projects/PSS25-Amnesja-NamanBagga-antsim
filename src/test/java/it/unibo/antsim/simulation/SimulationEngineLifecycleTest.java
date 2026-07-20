package it.unibo.antsim.simulation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SimulationEngineLifecycleTest {
    private SimulationEngine engine;

    @BeforeEach
    void setUp(){
        engine = new SimulationEngine();
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
