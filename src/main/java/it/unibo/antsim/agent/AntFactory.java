package it.unibo.antsim.agent;

import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.WorldPosition;

import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * This class is responsible for ant generation within designated simulation areas
 */
public class AntFactory {
    private final double defaultSpeed;
    private final RandomGenerator random;

    public AntFactory(double defaultSpeed, RandomGenerator random){
        if(defaultSpeed<=0){
            throw new IllegalArgumentException("Default speed must be a positive value");
        }
        this.defaultSpeed = defaultSpeed;
        this.random = Objects.requireNonNull(random, "Random generator cannot be null");
    }

    /**
     * Generate a new ant striclty inside boundries of specified nest cell
     * @param nestIndex         grid cell where nest is located
     * @param cellSizeX         width of a single cell in the grid
     * @param cellSizeY         height of a single cell in the grid
     * @return                  a newly created ant positioned randomly inside the cell with a random angle
     */
    public Ant generateAntInNest(CellIndex nestIndex, double cellSizeX, double cellSizeY){
        if(cellSizeX<0 || cellSizeY<0){
            throw new IllegalArgumentException("Cell sizes must be positive values");
        }
        Objects.requireNonNull(nestIndex, "Cell index cannot be null");

        // Calculates continuous coordinate bounds for the cell
        double minX = nestIndex.column() * cellSizeX;
        double maxX = minX + cellSizeX;
        double minY = nestIndex.row() * cellSizeY;
        double maxY = minY + cellSizeY;

        // Randomly generates a position within the cell
        double generateX = minX + random.nextDouble() * (maxX - minX);
        double generateY = minY + random.nextDouble() * (maxY - minY);

        // Randomly generates an orientation angle between 0 and 2pi
        double generateAngle = random.nextDouble() * 2 * Math.PI;

        return new Ant(new WorldPosition(generateX, generateY), generateAngle, defaultSpeed);
    }
}
