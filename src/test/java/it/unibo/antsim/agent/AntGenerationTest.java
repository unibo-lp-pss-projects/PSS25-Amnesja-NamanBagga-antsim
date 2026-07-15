package it.unibo.antsim.agent;

import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.WorldPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AntGenerationTest {
    private AntFactory factory;
    private static final double CELL_WIDTH = 10.0;
    private static final double CELL_HEIGHT = 15.0;
    private static final double ANT_SPEED = 2.5;

    @BeforeEach
    void setUp(){
        factory = new AntFactory(ANT_SPEED, new Random(12345)); // Fixed seed for deterministic behaviour
    }

    @Test
    void testAllAntsGenerationStrictlyInsideNestCell(){
        CellIndex nest = new CellIndex(2, 3);

        double minX = 30.0;
        double maxX = 40.0;
        double minY = 30.0;
        double maxY = 45.0;

        int generationCount = 1000;
        for(int i=0; i<generationCount; i++){
            Ant ant = factory.generateAntInNest(nest, CELL_WIDTH, CELL_HEIGHT);
            WorldPosition pos = ant.getPosition();

            // Verify position constraints
            assertTrue(pos.x() >= minX && pos.x() < maxX, "Ant's X position is out of bounds: " + pos.x()+ "(Expected [" + minX + ", " + maxX + "])");
            assertTrue(pos.y() >= minY && pos.y() < maxY, "Ant's Y position is out of bounds: " + pos.y()+ "(Expected [" + minY + ", " + maxY + "])");

            // Verify physical state constraints
            assertEquals(ANT_SPEED, ant.getSpeed(), 0.001, "Ant's speed is not as expected");
            assertTrue(ant.getAngle() >= 0.0 && ant.getAngle() < 2 * Math.PI, "Ant's angle is out of bounds: " + ant.getAngle());
        }
    }
}