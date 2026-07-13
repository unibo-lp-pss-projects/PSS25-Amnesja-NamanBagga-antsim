package it.unibo.antsim.world;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {
    private World world;
    private static final int ROWS = 5;
    private static final int COLS = 5;

    @BeforeEach
    void setUp(){
        world = new World(ROWS, COLS, 10.0, 10.0);
    }

    @Test
    void testNestRelocation(){
        CellIndex firstNest = new CellIndex(2, 2);
        CellIndex secondNest = new CellIndex(4, 4);

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
    void testGetWalkableNeighborsWithObstacles(){
        CellIndex center = new CellIndex(2, 2);
        CellIndex wall = new CellIndex(1, 2);

        // place obstacle near the center
        world.getGrid().getCellAt(wall).setCellContent(new CellContent.Obstacle());

        List<CellIndex> neighbors = world.getWalkableNeighbors(center);

        assertEquals(7, neighbors.size()); // There should be 7 walkable neighbors since one is blocked by the obstacle (3x3 grid)
        assertFalse(neighbors.contains(wall), "The obstacle cell should not be in the walkable neighbors"); // The obstacle cell should not be in the walkable neighbors
    }

    @Test
    void testPositionConversion(){
        WorldPosition posInCenter = new WorldPosition(25.5, 14.2); // This should be in column 2 (20-30) and row 1 (10-20)
        CellIndex expected = new CellIndex(1, 2);
        assertEquals(expected, world.convertToCellIndex(posInCenter));
    }

    @Test
    void testContinousSpaceQueries(){
        CellIndex wall = new CellIndex(0, 0);
        CellIndex food = new CellIndex(1, 1);
        CellIndex nest = new CellIndex(3, 3);

        world.getGrid().getCellAt(wall).setCellContent(new CellContent.Obstacle());
        world.getGrid().getCellAt(food).setCellContent(new CellContent.Food(100));
        world.relocateNest(nest);

        // continous coordinates inide the cells (10x10)
        WorldPosition wallPos = new WorldPosition(5.0, 5.0);
        WorldPosition foodPos = new WorldPosition(15.0, 15.0);
        WorldPosition nestPos = new WorldPosition(35.0, 35.0);
        WorldPosition outsidePos = new WorldPosition(-5.0, 20.0);

        // Verify isBlockedAt
        assertTrue(world.isBlockedAt(wallPos));
        assertFalse(world.isBlockedAt(foodPos));
        assertTrue(world.isBlockedAt(outsidePos), "Out of bounds should be considered blocked"); // Out of bounds should be considered blocked

        // Verify isFoodAt
        assertTrue(world.isFoodAt(foodPos));
        assertFalse(world.isFoodAt(wallPos));

        // Verify isNestAt
        assertTrue(world.isNestAt(nestPos));
        assertFalse(world.isNestAt(foodPos));
    }

    @Test
    void testFindFoodCellNear(){
        CellIndex current = new CellIndex(2, 2);
        CellIndex adjacent = new CellIndex(2, 3);

        WorldPosition agentPos = new WorldPosition(25.0, 25.0); // This corresponds to cell (2,2)

        // Case 1: No food nearby
        assertTrue(world.findFoodCellNear(agentPos).isEmpty());

        // Case 2: Food in an adjacent cell
        world.getGrid().setCellContent(adjacent, new CellContent.Food(50));
        var foodNear = world.findFoodCellNear(agentPos);
        assertTrue(foodNear.isPresent());
        assertEquals(adjacent, foodNear.get());

        // Case 3: Food in the same cell and the adjacent cell (the priority must be the current cell)
        world.getGrid().setCellContent(current, new CellContent.Food(100));
        var exactFoodPos = world.findFoodCellNear(agentPos);
        assertTrue(exactFoodPos.isPresent());
        assertEquals(current, exactFoodPos.get());

    }
}
