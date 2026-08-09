package it.unibo.antsim.world;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class WorldPositionAndCellIndexTest {

    @Test
    void testWorldPosition() {
        final WorldPosition p1 = new WorldPosition(10.5, 20.5);
        final WorldPosition p2 = new WorldPosition(10.5, 20.5);
        final WorldPosition p3 = new WorldPosition(0.0, 0.0);

        // Test record equality
        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
        assertEquals(p1.hashCode(), p2.hashCode());

        // Test mathematical methods
        final double x = 15.5;
        final double y = 22.0;
        final double x1 = 5.0;
        final double y1 = 1.5;
        assertEquals(new WorldPosition(x, y), p1.add(x1, y1));
        assertEquals(x1, new WorldPosition(3.0, 0.0).distanceToAnotherPosition(new WorldPosition(0.0, 4.0)));
    }

    @Test
    void testCellIndex() {
        final int r = 5;
        final int c = 10;
        final CellIndex c1 = new CellIndex(r, c);
        final CellIndex c2 = new CellIndex(r, c);
        final CellIndex c3 = new CellIndex(0, 0);

        assertEquals(c1, c2);
        assertNotEquals(c1, c3);
        assertEquals(c1.hashCode(), c2.hashCode());

        assertEquals(r, c1.row());
        assertEquals(10, c1.column());
    }
}
