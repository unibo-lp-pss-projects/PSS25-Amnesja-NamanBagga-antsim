package it.unibo.antsim.agent;

import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.WorldPosition;

import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * This class is responsible for ant generation within designated simulation areas.
 */
public class AntFactory {
    private static final double EXPLORER_RATIO = 0.50;
    private final double defaultSpeed;
    private final RandomGenerator random;

    /**
     * Instantiates a new Ant factory.
     *
     * @param defaultSpeed the default speed
     * @param random the random
     */
    public AntFactory(final double defaultSpeed, final RandomGenerator random) {
        if (defaultSpeed <= 0) {
            throw new IllegalArgumentException("Default speed must be a positive value");
        }
        this.defaultSpeed = defaultSpeed;
        this.random = Objects.requireNonNull(random, "Random generator cannot be null");
    }

    /**
     * Generate a new ant strictly inside boundaries of specified nest cell.
     *
     * @param nestIndex grid cell where nest is located
     * @param cellSizeX width of a single cell in the grid
     * @param cellSizeY height of a single cell in the grid
     * @return a newly created ant positioned randomly inside the cell with a random angle
     */
    public Ant generateAntInNest(final CellIndex nestIndex, final double cellSizeX, final double cellSizeY) {
        if (cellSizeX < 0 || cellSizeY < 0) {
            throw new IllegalArgumentException("Cell sizes must be positive values");
        }
        Objects.requireNonNull(nestIndex, "Cell index cannot be null");

        // Continuous coordinate calculations.
        final double minX = nestIndex.column() * cellSizeX;
        final double maxX = minX + cellSizeX;
        final double minY = nestIndex.row() * cellSizeY;
        final double maxY = minY + cellSizeY;

        // Randomly generates a position withing the cell.
        final double generateX = minX + random.nextDouble() * (maxX - minX);
        final double generateY = minY + random.nextDouble() * (maxY - minY);

        // Randomly generates an angle between 0 and 2pi
        final double generateAngle = random.nextDouble() * 2 * Math.PI;

        final AntRole role = random.nextDouble() < EXPLORER_RATIO ? AntRole.EXPLORER : AntRole.FOLLOWER;
        return new Ant(new WorldPosition(generateX, generateY), generateAngle, defaultSpeed, role);
    }
}
