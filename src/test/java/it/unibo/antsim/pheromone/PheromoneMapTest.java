package it.unibo.antsim.pheromone;

import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.WorldPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PheromoneMapTest {
    private static final int ROW = 4;
    private static final int COL = 4;
    private static final double CELL_SIZE = 10.0;
    private static final double MAX_SATURATION = 100.0;
    private static final double DECAY_RATE = 0.3;
    private PheromoneMap pheromoneMap;

    @BeforeEach
    void setUp() {
        pheromoneMap = new PheromoneMap(
                ROW, COL, CELL_SIZE, CELL_SIZE, MAX_SATURATION, new Evaporation(DECAY_RATE)
        );
    }

    @Test
    void testSaturationNeverExceedsMax() {
        final CellIndex index = new CellIndex(1, 1);
        final double intensity = 60.0;
        pheromoneMap.deposit(index, PheromoneField.PheromoneType.HOME, intensity);
        pheromoneMap.deposit(index, PheromoneField.PheromoneType.HOME, intensity);

        final double level = pheromoneMap.level(new WorldPosition(15.0, 15.0), PheromoneField.PheromoneType.HOME);
        assertEquals(MAX_SATURATION, level, "The pheromone level has exceeded the maximum saturation");
    }

    @Test
    void interpolatedValueAtCellMidPoint() {
        // 4 adjacent values forming a square
        final double intensity1 = 10.0;
        final double intensity2 = 20.0;
        final double intensity3 = 30.0;
        final double intensity4 = 40.0;
        pheromoneMap.deposit(new CellIndex(1, 1), PheromoneField.PheromoneType.FOOD, intensity1);
        pheromoneMap.deposit(new CellIndex(1, 2), PheromoneField.PheromoneType.FOOD, intensity2);
        pheromoneMap.deposit(new CellIndex(2, 1), PheromoneField.PheromoneType.FOOD, intensity3);
        pheromoneMap.deposit(new CellIndex(2, 2), PheromoneField.PheromoneType.FOOD, intensity4);

        final WorldPosition midPoint = new WorldPosition(20.0, 20.0);
        final double interpolatedLevel = pheromoneMap.level(midPoint, PheromoneField.PheromoneType.FOOD);

        // matematic media (10+20+30+40)/4 = 25
        final double exceptedIntensity = 25.0;
        assertEquals(exceptedIntensity, interpolatedLevel, "The interpolated pheromone level at the midpoint is incorrect");
    }

    @Test
    void evaporationFollowsExponentialDecay() {
        final CellIndex index = new CellIndex(0, 0);
        pheromoneMap.deposit(index, PheromoneField.PheromoneType.HOME, 0.5);

        // first evaporation
        pheromoneMap.evaporate(1.0);
        final double level1 = pheromoneMap.level(new WorldPosition(5.0, 5.0), PheromoneField.PheromoneType.HOME);
        assertEquals(0.5 * Math.exp(-DECAY_RATE), level1);

        // second evaporation
        pheromoneMap.evaporate(1.0);
        final double level2 = pheromoneMap.level(new WorldPosition(5.0, 5.0), PheromoneField.PheromoneType.HOME);
        final double value = -2;
        assertEquals(0.5 * Math.exp(value * DECAY_RATE), level2);
    }
}
