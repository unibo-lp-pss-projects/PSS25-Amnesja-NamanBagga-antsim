package it.unibo.antsim.agent;

import it.unibo.antsim.world.CellContent;
import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.World;
import it.unibo.antsim.world.WorldPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AntMovementTest {
    private static final double CELL_SIZE = 10.0;
    private World world;

    @BeforeEach
    void setUp() {
        final int r = 5;
        final int c = 5;
        // Creating small world
        world = new World(r, c, CELL_SIZE, CELL_SIZE);
    }

    @Test
    void testContinuousMovementInFreeSpace() {
        final Ant ant = new Ant(new WorldPosition(25.0, 25.0), 0.0, 2.0, AntRole.FOLLOWER);

        // move 1 time tick
        ant.move(1.0, world);

        // Expected position after moving 1 tick
        final double expectedPos1 = 27.0;
        final double expectedPos2 = 25.0;
        assertEquals(expectedPos1, ant.getPosition().x());
        assertEquals(expectedPos2, ant.getPosition().y());
    }

    @Test
    void testMovementCollisionWithWalls() {
        // creating an obstacle
        world.getGrid().setCellContent(new CellIndex(2, 3), new CellContent.Obstacle());

        // positioning the ant right in front of the obstacle
        final Ant ant = new Ant(new WorldPosition(29.0, 25.0), 0.0, 2.0, AntRole.FOLLOWER);

        ant.move(1.0, world);

        // The ant should not have moved into the obstacle
        final double notExpectedPos = 31.0;
        assertNotEquals(notExpectedPos, ant.getPosition().x());
        final double conditionPos = 30.0;
        assertTrue(ant.getPosition().x() <= conditionPos, "The ant walked on the obstacle!!!");

        // The ant should have bounced back, so its angle should have changed
        assertNotEquals(0.0, ant.getAngle(), "The ant did not bounce");
    }
}
