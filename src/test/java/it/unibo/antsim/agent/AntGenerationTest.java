package it.unibo.antsim.agent;

import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.WorldPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AntGenerationTest {
    private static final double CELL_WIDTH = 10.0;
    private static final double CELL_HEIGHT = 15.0;
    private static final double ANT_SPEED = 2.5;
    private AntFactory factory;

    @BeforeEach
    void setUp() {
        final long seed = 42L;
        factory = new AntFactory(ANT_SPEED, new Random(seed)); // Fixed seed for deterministic behaviour
    }

    @Test
    void testAllAntsGenerationStrictlyInsideNestCell() {
        final CellIndex nest = new CellIndex(2, 3);

        final double minX = 30.0;
        final double maxX = 40.0;
        final double minY = 30.0;
        final double maxY = 45.0;

        for (int i = 0; i < 1000; i++) {
            final Ant ant = factory.generateAntInNest(nest, CELL_WIDTH, CELL_HEIGHT);
            final WorldPosition pos = ant.getPosition();

            // Verify position constraints
            assertTrue(pos.x() >= minX && pos.x() < maxX,
                    "Ant's X position is out of bounds: " + pos.x() + "(Expected [" + minX + ", " + maxX + "])");

            assertTrue(pos.y() >= minY && pos.y() < maxY,
                    "Ant's Y position is out of bounds: " + pos.y() + "(Expected [" + minY + ", " + maxY + "])");

            // Verify physical state constraints
            assertEquals(ANT_SPEED, ant.getSpeed(), "Ant's speed is not as expected");
            assertTrue(ant.getAngle() >= 0.0 && ant.getAngle() < 2 * Math.PI, "Ant's angle is out of bounds: " + ant.getAngle());
        }
    }
}
