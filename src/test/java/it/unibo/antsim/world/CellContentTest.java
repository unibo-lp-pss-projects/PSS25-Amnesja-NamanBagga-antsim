package it.unibo.antsim.world;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CellContentTest {

    @Test
    void testFoodValidation() {
        // Quantity validation
        assertDoesNotThrow(() -> new CellContent.Food(100));

        // Exception on negative quantity
        assertThrows(IllegalArgumentException.class, () -> new CellContent.Food(-1));
    }

    @Test
    void testFoodConsume() {
        final CellContent.Food food = new CellContent.Food(50);

        // Consume a valid amount
        final CellContent.Food remainingFood = food.consume(20);
        final int expected = 30;
        assertEquals(expected, remainingFood.quantity());
        assertFalse(remainingFood.isEmpty());

        // Total consumption or excessive consumption
        final CellContent.Food emptyFood = remainingFood.consume(40);
        assertEquals(0, emptyFood.quantity());
        assertTrue(emptyFood.isEmpty());
    }

    @Test
    void testFoodConsumeValidation() {
        final int amount = 10;
        final int amount2 = -5;

        final CellContent.Food food = new CellContent.Food(amount);
        assertThrows(IllegalArgumentException.class, () -> {
            final CellContent.Food result = food.consume(amount2);
            assertTrue(result.isEmpty());
        });
    }
}
