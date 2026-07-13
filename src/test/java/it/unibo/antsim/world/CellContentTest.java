package it.unibo.antsim.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellContentTest {

    @Test
    void testFoodValidation() {
        // Quantity validation
        assertDoesNotThrow(() -> new CellContent.Food(100));

        // Exception on negative quantity
        assertThrows(IllegalArgumentException.class, () -> new CellContent.Food(-1));
    }

    @Test
    void testFoodConsume(){
        CellContent.Food food = new CellContent.Food(50);

        // Consume a valid amount
        CellContent.Food remainingFood = food.consume(20);
        assertEquals(30, remainingFood.quantity());
        assertFalse(remainingFood.isEmpty());

        // Total consumption or excessive consumption
        CellContent.Food emptyFood = remainingFood.consume(40);
        assertEquals(0, emptyFood.quantity());
        assertTrue(emptyFood.isEmpty());
    }

    @Test
    void testFoodConsumeValidation(){
        CellContent.Food food = new CellContent.Food(10);
        assertThrows(IllegalArgumentException.class, () -> food.consume(-5));
    }
}
