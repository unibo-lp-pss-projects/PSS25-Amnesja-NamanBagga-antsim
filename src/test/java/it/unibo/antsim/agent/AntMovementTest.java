package it.unibo.antsim.agent;

import it.unibo.antsim.world.CellContent;
import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.World;
import it.unibo.antsim.world.WorldPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AntMovementTest {
    private World world;
    private static final double CELL_SIZE = 10.0;

    @BeforeEach
    void setUp(){
        // Creating small world
        world = new World(5, 5,CELL_SIZE, CELL_SIZE);
    }

    @Test
    void TestContinuosMovementInFreeSpace(){
        Ant ant = new Ant(new WorldPosition(25.0, 25.0), 0.0, 2.0);

        // move 1 time tick
        ant.move(1.0, world);

        // Expected position after moving 1 tick
        assertEquals(27.0, ant.getPosition().x(), 0.001);
        assertEquals(25.0, ant.getPosition().y(), 0.001);
    }

    @Test
    void testMovementCollisionWithWalls(){
        // creating an obstacle
        world.getGrid().setCellContent(new CellIndex(2, 3), new CellContent.Obstacle());

        // positioning the ant right in front of the obstacle
        Ant ant = new Ant(new WorldPosition(29.0, 25.0), 0.0, 2.0);

        ant.move(1.0, world);

        // The ant should not have moved into the obstacle
        assertNotEquals(31.0, ant.getPosition().x());
        assertTrue(ant.getPosition().x() <= 30.0, "The ant walked on the obstacle!!!");

        // The ant should have bounced back, so its angle should have changed
        assertNotEquals(0.0, ant.getAngle(), "The ant did not bounce");
    }
}
