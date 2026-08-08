package it.unibo.antsim.world;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class GridTest {
    private static final int ROWS = 10;
    private static final int COLS = 15;
    private Grid grid;

    @BeforeEach
    void setUp() {
        grid = new Grid(ROWS, COLS);
    }

    @Test
    void testGridInitialization() {
        assertEquals(ROWS, grid.getRows());
        assertEquals(COLS, grid.getColumns());

        // Verifiy if the default inizialization is Empty
        assertInstanceOf(CellContent.Empty.class, grid.getCellAt(new CellIndex(0, 0)).getCellContent());
    }

    @Test
    void testInsideEdgeCases() {
        final int r = 5;
        final int c = 5;
        // Inside
        assertTrue(grid.isInside(new CellIndex(0, 0)));
        assertTrue(grid.isInside(new CellIndex(ROWS - 1, COLS - 1)));
        assertTrue(grid.isInside(new CellIndex(r, c)));

        // Edge cases
        assertFalse(grid.isInside(new CellIndex(-1, 0)));
        assertFalse(grid.isInside(new CellIndex(0, -1)));
        assertFalse(grid.isInside(new CellIndex(ROWS, COLS - 1)));
        assertFalse(grid.isInside(new CellIndex(ROWS - 1, COLS)));
    }

    @Test
    void testGetAndSetCell() {
        final CellIndex index = new CellIndex(3, 4);
        final CellContent.Food food = new CellContent.Food(10);

        grid.setCellContent(index, food);
        assertEquals(food, grid.getCellAt(index).getCellContent());
    }

    @Test
    void testOutOfBoundsException() {
        final CellIndex invalidIndex = new CellIndex(ROWS, COLS); // Out of bounds
        assertThrows(IndexOutOfBoundsException.class, () -> {
            final Cell result = grid.getCellAt(invalidIndex);
            assertNull(result);
        });
    }
}
