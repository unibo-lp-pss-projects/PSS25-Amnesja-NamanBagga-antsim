package it.unibo.antsim.world;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GridTest {
    private Grid grid;
    private static final int ROWS = 10;
    private static final int COLS = 15;

    @BeforeEach
    void setUp(){
        grid = new Grid(ROWS, COLS);
    }

    @Test
    void testGridInizialization(){
        assertEquals(ROWS, grid.getRows());
        assertEquals(COLS, grid.getColumns());

        // Verifiy if the default inizialization is Empty
        assertInstanceOf(CellContent.Empty.class, grid.getCellAt(new CellIndex(0, 0)).getCellContent());
    }

    @Test
    void testInsideEdgeCases(){
        // Inside
        assertTrue(grid.isInside(new CellIndex(0, 0)));
        assertTrue(grid.isInside(new CellIndex(ROWS - 1, COLS - 1)));
        assertTrue(grid.isInside(new CellIndex(5, 5)));

        // Edge cases
        assertFalse(grid.isInside(new CellIndex(-1, 0)));
        assertFalse(grid.isInside(new CellIndex(0, -1)));
        assertFalse(grid.isInside(new CellIndex(ROWS, COLS-1)));
        assertFalse(grid.isInside(new CellIndex(ROWS-1, COLS)));
    }

    @Test
    void testGetAndSetCell(){
        CellIndex index = new CellIndex(3, 4);
        CellContent.Food food= new CellContent.Food(10);

        grid.setCellContent(index, food);
        assertEquals(food, grid.getCellAt(index).getCellContent());
    }

    @Test
    void testOutOfBoundsException(){
        CellIndex invalidIndex = new CellIndex(ROWS, COLS); // Out of bounds
        assertThrows(IndexOutOfBoundsException.class, () -> grid.getCellAt(invalidIndex));
    }
}
