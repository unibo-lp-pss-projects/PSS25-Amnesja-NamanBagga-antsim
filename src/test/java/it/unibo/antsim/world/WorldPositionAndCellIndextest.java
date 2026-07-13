package it.unibo.antsim.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class WorldPositionAndCellIndextest {

    @Test
    void testWorldPosition() {
        WorldPosition p1 = new WorldPosition(10.5, 20.5);
        WorldPosition p2 = new WorldPosition(10.5, 20.5);
        WorldPosition p3 = new WorldPosition(0.0, 0.0);

        // Test record equality
        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
        assertEquals(p1.hashCode(), p2.hashCode());

        // Test mathematical methods
        assertEquals(new WorldPosition(15.5, 22.0), p1.add(5.0, 1.5));
        assertEquals(5.0, new WorldPosition(3.0, 0.0).distanceToAnotherPosition(new WorldPosition(0.0, 4.0)), 0.001);
    }

    @Test
    void testCellIndex() {
        CellIndex c1 = new CellIndex(5, 10);
        CellIndex c2 = new CellIndex(5, 10);
        CellIndex c3 = new CellIndex(0, 0);

        assertEquals(c1, c2);
        assertNotEquals(c1, c3);
        assertEquals(c1.hashCode(), c2.hashCode());

        assertEquals(5, c1.row());
        assertEquals(10, c1.column());
    }
}
