package it.unibo.antsim.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CellTest {

    @Test
    void testInitialContent(){
        Cell c = new Cell(new CellContent.Obstacle());
        assertTrue(c.getCellContent() instanceof CellContent.Obstacle);
    }

    @Test
    void testContentTransition(){
        Cell c = new Cell(new CellContent.Empty());

        // Manual change of cell content
        c.setCellContent(new CellContent.Nest());
        assertTrue(c.getCellContent() instanceof CellContent.Nest);
    }

    @Test
    void testFoodConsumptionAndTransitionToEmpty(){
        Cell c = new Cell(new CellContent.Food(30));

        // Partial consumption: the cell should still contain food
        c.consumeFood(10);
        assertInstanceOf(CellContent.Food.class, c.getCellContent());
        assertEquals(20, ((CellContent.Food) c.getCellContent()).quantity());

        // Total consumption or excessive consumption: the cell should transition to Empty
        c.consumeFood(25);
        assertInstanceOf(CellContent.Empty.class, c.getCellContent());
    }

    @Test
    void testConsumeFoodOnNonFoodCell(){
        Cell c = new Cell(new CellContent.Obstacle());

        // Consuming food on a non-food cell should not change the content
        assertDoesNotThrow(() -> c.consumeFood(10));
        assertTrue(c.getCellContent() instanceof CellContent.Obstacle);
    }
}
