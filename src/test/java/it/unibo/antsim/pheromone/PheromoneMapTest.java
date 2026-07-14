package it.unibo.antsim.pheromone;

import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.WorldPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PheromoneMapTest {
    private PheromoneMap pheromoneMap;
    private static final int ROW = 4;
    private static final int COL = 4;
    private static final double CELL_SIZE = 10.0;
    private static final double MAX_SATURATION = 100.0;
    private static final double DECAY_RATE = 0.3;

    @BeforeEach
    void setUp() {
        pheromoneMap = new PheromoneMap(
                ROW, COL, CELL_SIZE, CELL_SIZE, MAX_SATURATION, new Evaporation(DECAY_RATE)
        );
    }

    @Test
    void testSaturationNeverExceedsMax(){
        CellIndex index = new CellIndex(1, 1);

        pheromoneMap.deposit(index, PheromoneField.PheromoneType.HOME, 60.0);
        pheromoneMap.deposit(index, PheromoneField.PheromoneType.HOME, 60.0);

        double level = pheromoneMap.level(new WorldPosition(15.0, 15.0), PheromoneField.PheromoneType.HOME);
        assertEquals(MAX_SATURATION, level, "The pheromone level has exceeded the maximum saturation");
    }

    @Test
    void interpolatedValueAtCellMidPoint(){
        // 4 adjacent values forming a square
        pheromoneMap.deposit(new CellIndex(1, 1), PheromoneField.PheromoneType.FOOD, 10.0);
        pheromoneMap.deposit(new CellIndex(1, 2), PheromoneField.PheromoneType.FOOD, 20.0);
        pheromoneMap.deposit(new CellIndex(2, 1), PheromoneField.PheromoneType.FOOD, 30.0);
        pheromoneMap.deposit(new CellIndex(2, 2), PheromoneField.PheromoneType.FOOD, 40.0);

        WorldPosition midPoint = new WorldPosition(20.0, 20.0);
        double interpolatedLevel = pheromoneMap.level(midPoint, PheromoneField.PheromoneType.FOOD);

        // matematic media (10+20+30+40)/4 = 25
        assertEquals(25.0, interpolatedLevel, "The interpolated pheromone level at the midpoint is incorrect");
    }

    @Test
    void evaporationTrendsToZero(){
        CellIndex index = new CellIndex(0, 0);
        pheromoneMap.deposit(index, PheromoneField.PheromoneType.HOME, 0.5);

        // first evaporation
        pheromoneMap.evaporate();
        double level1 = pheromoneMap.level(new WorldPosition(5.0, 5.0), PheromoneField.PheromoneType.HOME);
        assertEquals(0.2, level1, 0.001);

        // second evaporation
        pheromoneMap.evaporate();
        double level2 = pheromoneMap.level(new WorldPosition(5.0, 5.0), PheromoneField.PheromoneType.HOME);
        assertEquals(0.0, level2, "The evaporation is below zero!!!!");
    }
}
