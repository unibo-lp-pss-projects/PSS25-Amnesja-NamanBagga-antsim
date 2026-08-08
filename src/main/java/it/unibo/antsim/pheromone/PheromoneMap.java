package it.unibo.antsim.pheromone;

import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.WorldPosition;
import java.util.Arrays;
import java.util.Objects;

/**
 * Implementation of grid map storing and updating pheromones intensity.
 */
public class PheromoneMap implements PheromoneField {
    private final int rows;
    private final int cols;
    private final double cellWidth;
    private final double cellHeight;
    private final double maxSaturation;
    private final EvaporationModel evaporationModel;

    private final double[][] foodPheromones;
    private final double[][] homePheromones;

    /**
     * Instantiates a new Pheromone map.
     *
     * @param rows number of rows
     * @param cols number of columns
     * @param cellWidth each cell width
     * @param cellHeight each cell height
     * @param maxSaturation the max saturation
     * @param evaporationModel the evaporation model
     */
    public PheromoneMap(
            final int rows,
            final int cols,
            final double cellWidth,
            final double cellHeight,
            final double maxSaturation,
            final EvaporationModel evaporationModel
    ) {
        if (rows <= 0 || cols <= 0 || cellWidth <= 0 || cellHeight <= 0 || maxSaturation <= 0) {
            throw new IllegalArgumentException("Arguments must be positive");
        }
        this.rows = rows;
        this.cols = cols;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.maxSaturation = maxSaturation;
        this.evaporationModel = Objects.requireNonNull(evaporationModel);
        this.foodPheromones = new double[rows][cols];
        this.homePheromones = new double[rows][cols];
    }

    /**
     * Deposits pheromones at a specific cell index.
     *
     * @param index cell index where the pheromone is deposited
     * @param type pheromone type
     * @param intensity intensity of the pheromone
     */
    @Override
    public void deposit(final CellIndex index, final PheromoneType type, final double intensity) {
        if (index.row() >= 0 && index.row() < rows && index.column() >= 0 && index.column() < cols) {
            final double[][] grid = (type == PheromoneType.HOME) ? homePheromones : foodPheromones;
            grid[index.row()][index.column()] = Math.min(maxSaturation, grid[index.row()][index.column()] + intensity);
        }
    }

    /**
     * Applies evaporation decay over time to all cells.
     *
     * @param dt time step duration value
     */
    @Override
    public void evaporate(final double dt) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                foodPheromones[r][c] = evaporationModel.decay(foodPheromones[r][c], dt);
                homePheromones[r][c] = evaporationModel.decay(homePheromones[r][c], dt);
            }
        }
    }

    /**
     * Clears all pheromones.
     */
    @Override
    public void clear() {
        for (int row = 0; row < rows; row++) {
            Arrays.fill(foodPheromones[row], 0.0);
            Arrays.fill(homePheromones[row], 0.0);
        }
    }

    /**
     * Samples the pheromone level at continuous world position.
     *
     * @param pos the position in the world to sample
     * @param type type of pheromone
     * @return the pheromone intensity/level
     */
    @Override
    public double level(final WorldPosition pos, final PheromoneType type) {
        final double centerX = (pos.x() / cellWidth) - 0.5;
        final double centerY = (pos.y() / cellHeight) - 0.5;

        final int c0 = (int) Math.floor(centerX);
        final int r0 = (int) Math.floor(centerY);

        final double a = centerX - c0;
        final double b = centerY - r0;

        final double v00 = safeRead(r0, c0, type);
        final double v10 = safeRead(r0, c0 + 1, type);
        final double v01 = safeRead(r0 + 1, c0, type);
        final double v11 = safeRead(r0 + 1, c0 + 1, type);

        return (1 - a) * (1 - b) * v00 + a * (1 - b) * v10 + (1 - a) * b * v01 + a * b * v11;
    }

    /**
     * Safely read the pheromone levels at a specifies row and column with clamping.
     *
     * @param r row index
     * @param c column indec
     * @param type type of pheromone
     * @return pheromone level at a clamped grid coordinate
     */
    private double safeRead(final int r, final int c, final PheromoneType type) {
        final int clampedRow = Math.clamp(r, 0, rows - 1);
        final int clampedCol = Math.clamp(c, 0, cols - 1);
        return (type == PheromoneType.HOME) ? homePheromones[clampedRow][clampedCol] : foodPheromones[clampedRow][clampedCol];
    }
}
