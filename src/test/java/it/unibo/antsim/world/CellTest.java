package it.unibo.antsim.world;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CellTest {

    @Test
    void testInitialContent() {
        final Cell c = new Cell(new CellContent.Obstacle());
        assertTrue(c.getCellContent() instanceof CellContent.Obstacle);
    }

    @Test
    void testContentTransition() {
        final Cell c = new Cell(new CellContent.Empty());

        // Manual change of cell content
        c.setCellContent(new CellContent.Nest());
        assertTrue(c.getCellContent() instanceof CellContent.Nest);
    }

    @Test
    void testFoodConsumptionAndTransitionToEmpty() {
        final Cell c = new Cell(new CellContent.Food(30));

        // Partial consumption: the cell should still contain food
        c.consumeFood(10);
        assertInstanceOf(CellContent.Food.class, c.getCellContent());

        final int expectedFood = 20;
        assertEquals(expectedFood, ((CellContent.Food) c.getCellContent()).quantity());

        // Total consumption or excessive consumption: the cell should transition to Empty
        final int amount = 25;
        c.consumeFood(amount);
        assertInstanceOf(CellContent.Empty.class, c.getCellContent());
    }

    @Test
    void testConsumeFoodOnNonFoodCell() {
        final Cell c = new Cell(new CellContent.Obstacle());

        // Consuming food on a non-food cell should not change the content
        assertDoesNotThrow(() -> c.consumeFood(10));
        assertTrue(c.getCellContent() instanceof CellContent.Obstacle);
    }
}
