package it.unibo.antsim.world;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class WorldTest {
    private static final int ROWS = 5;
    private static final int COLS = 5;
    private World world;

    @BeforeEach
    void setUp() {
        world = new World(ROWS, COLS, 10.0, 10.0);
    }

    @Test
    void testNestRelocation() {
        final CellIndex firstNest = new CellIndex(2, 2);
        final CellIndex secondNest = new CellIndex(4, 4);

        // Initial nest position
        world.relocateNest(firstNest);
        assertEquals(firstNest, world.getNestIndex());
        assertInstanceOf(CellContent.Nest.class, world.getGrid().getCellAt(firstNest).getCellContent());

        // Relocate nest to a new position
        world.relocateNest(secondNest);
        assertEquals(secondNest, world.getNestIndex());
        assertInstanceOf(CellContent.Empty.class, world.getGrid().getCellAt(firstNest).getCellContent());
        assertInstanceOf(CellContent.Nest.class, world.getGrid().getCellAt(secondNest).getCellContent());
    }

    @Test
    void testGetWalkableNeighborsWithObstacles() {
        final CellIndex center = new CellIndex(2, 2);
        final CellIndex wall = new CellIndex(1, 2);

        // place obstacle near the center
        world.getGrid().getCellAt(wall).setCellContent(new CellContent.Obstacle());

        final List<CellIndex> neighbors = world.getWalkableNeighbors(center);
        final int expectedCell = 7;
        // There should be 7 walkable neighbors since one is blocked by the obstacle (3x3 grid)
        assertEquals(expectedCell, neighbors.size());
        // The obstacle cell should not be in the walkable neighbors
        assertFalse(neighbors.contains(wall), "The obstacle cell should not be in the walkable neighbors");
    }

    @Test
    void testPositionConversion() {
        // This should be in column 2 (20-30) and row 1 (10-20)
        final WorldPosition posInCenter = new WorldPosition(25.5, 14.2);
        final CellIndex expected = new CellIndex(1, 2);
        assertEquals(expected, world.convertToCellIndex(posInCenter));
    }

    @Test
    void testContinuousSpaceQueries() {
        final CellIndex wall = new CellIndex(0, 0);
        final CellIndex food = new CellIndex(1, 1);
        final CellIndex nest = new CellIndex(3, 3);

        world.getGrid().getCellAt(wall).setCellContent(new CellContent.Obstacle());
        world.getGrid().getCellAt(food).setCellContent(new CellContent.Food(100));
        world.relocateNest(nest);

        // Continuous coordinates inide the cells (10x10)
        final WorldPosition wallPos = new WorldPosition(5.0, 5.0);
        final WorldPosition foodPos = new WorldPosition(15.0, 15.0);
        final WorldPosition nestPos = new WorldPosition(35.0, 35.0);
        final WorldPosition outsidePos = new WorldPosition(-5.0, 20.0);

        // Verify isBlockedAt
        assertTrue(world.isBlockedAt(wallPos));
        assertFalse(world.isBlockedAt(foodPos));

        // Out of bounds should be considered blocked
        assertTrue(world.isBlockedAt(outsidePos), "Out of bounds should be considered blocked");

        // Verify isFoodAt
        assertTrue(world.isFoodAt(foodPos));
        assertFalse(world.isFoodAt(wallPos));

        // Verify isNestAt
        assertTrue(world.isNestAt(nestPos));
        assertFalse(world.isNestAt(foodPos));
    }

    @Test
    void testFindFoodCellNear() {
        final CellIndex current = new CellIndex(2, 2);
        final CellIndex adjacent = new CellIndex(2, 3);

        final WorldPosition agentPos = new WorldPosition(25.0, 25.0); // This corresponds to cell (2,2)
        final int foodQ1 = 50;
        final int foodQ2 = 100;

        // Case 1: No food nearby
        assertTrue(world.findFoodCellNear(agentPos).isEmpty());

        // Case 2: Food in an adjacent cell
        world.getGrid().setCellContent(adjacent, new CellContent.Food(foodQ1));
        final var foodNear = world.findFoodCellNear(agentPos);
        assertTrue(foodNear.isPresent());
        assertEquals(adjacent, foodNear.get());

        // Case 3: Food in the same cell and the adjacent cell (the priority must be the current cell)
        world.getGrid().setCellContent(current, new CellContent.Food(foodQ2));
        final var exactFoodPos = world.findFoodCellNear(agentPos);
        assertTrue(exactFoodPos.isPresent());
        assertEquals(current, exactFoodPos.get());
    }
}
