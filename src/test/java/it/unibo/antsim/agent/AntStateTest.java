package it.unibo.antsim.agent;

import it.unibo.antsim.world.WorldPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AntStateTest {
    private Ant ant;

    @BeforeEach
    void setUp() {
        ant = new Ant(new WorldPosition(10.0, 10.0), 0.0, 2.0, AntRole.FOLLOWER);
    }

    @Test
    void testInitialStateIsWandering() {
        assertEquals(AntState.WANDERING, ant.getState(), "Initial state should be WANDERING");
    }

    @Test
    void testTransitionPickAndDrop() {
        // pick up food: state = RETURING_TO_NEST
        ant.pickFood();
        assertEquals(AntState.RETURNING_TO_NEST, ant.getState(), "State should be RETURNING_TO_NEST after picking up food");
        assertTrue(ant.isCarryingFood(), "Ant should be carrying food after picking it up");

        // drop food: state = WANDERING
        ant.dropFood();
        assertEquals(AntState.WANDERING, ant.getState(), "State should be WANDERING after dropping food");
        assertFalse(ant.isCarryingFood(), "Ant should not be carrying food after dropping it");
    }

    @Test
    void testInvalidTransitionThrowException() {
        // can't drop the food is not carrying it (state = WANDERING)
        assertThrows(IllegalStateException.class, ant::dropFood);

        ant.pickFood();
        //can't pick up food if it's already carrying it (state = RETURNING_TO_NEST)
        assertThrows(IllegalStateException.class, ant::pickFood);
    }
}
