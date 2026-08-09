package it.unibo.antsim.world.generation;

import it.unibo.antsim.world.CellContent;
import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.World;

import java.util.random.RandomGenerator;

/**
 * Manages the world generation for the simulation.
 */
public class WorldGenerator {
    private static final int ROCK_CLUSTER_MIN_OFFSET = -2;
    private static final int ROCK_CLUSTER_MAX_OFFSET = 2;
    private static final int FOOD_CLUSTER_MIN_OFFSET = 0;
    private static final int FOOD_CLUSTER_MAX_OFFSET = 1;

    private final RandomGenerator random;

    /**
     * Instantiates a new World generator.
     *
     * @param random the random
     */
    public WorldGenerator(final RandomGenerator random) {
        this.random = random;       // Initialize the random source
    }

    /**
     * Generates a new world with a nest, obstacles and food cluster.
     *
     * @param params the generation parameters
     * @return the generated world
     */
    public World generate(final GenerationParameters params) {
        final World world = new World(params.rows(), params.cols(), params.cellWidth(), params.cellHeight());

        // nest position at the center of the world as a default
        final CellIndex nestIndex = new CellIndex(params.rows() / 2, params.cols() / 2);
        world.relocateNest(nestIndex);

        // Generation of the obstacles cluster
        generateRockClusters(world, nestIndex, params);

        // Generation of the food cluster
        generateFoodClusters(world, nestIndex, params);

        return world;
    }

    /**
     * Generates rock obstacles clusters.
     *
     * @param world the world instance
     * @param nestIndex the nest index position
     * @param params the generation parameters
     */
    private void generateRockClusters(final World world, final CellIndex nestIndex, final GenerationParameters params) {
        for (int i = 0; i < params.numRockClusters(); i++) {
            // Initial starting random for the cluster
            final int startRow = random.nextInt(params.rows());
            final int startCol = random.nextInt(params.cols());
            final CellIndex center = new CellIndex(startRow, startCol);

            // Expansion of the cluster around at the center
            for (int dr = ROCK_CLUSTER_MIN_OFFSET; dr <= ROCK_CLUSTER_MAX_OFFSET; dr++) {
                for (int dc = ROCK_CLUSTER_MIN_OFFSET; dc <= ROCK_CLUSTER_MAX_OFFSET; dc++) {
                   final CellIndex target = new CellIndex(center.row() + dr, center.column() + dc);

                    if (world.getGrid().isInside(target)) {
                        // Verifiy id the cell is near the nest
                        if (isInsideClearanceRadius(target, nestIndex, params.nestClearanceRadius())) {
                            continue;       // Skip for nest protection (avoiding obstacles very close to the nest)
                        }

                        // Probability of rock generation decreasing with how far is from the center
                        final double distanceModifier = 1.0 / (1.0 + Math.hypot(dr, dc));
                        if (random.nextDouble() < (params.rockProbability() * distanceModifier)) {
                            world.getGrid().setCellContent(target, new CellContent.Obstacle());
                        }
                    }
                }
            }
        }
    }

    /**
     * Generates food clusters.
     *
     * @param world the world instance
     * @param nestIndex the nest index position
     * @param params the generation parameters
     */
    private void generateFoodClusters(final World world, final CellIndex nestIndex, final GenerationParameters params) {
        for (int i = 0; i < params.numFoodClusters(); i++) {
            final int startRow = random.nextInt(params.rows());
            final int startCol = random.nextInt(params.cols());
            final CellIndex center = new CellIndex(startRow, startCol);

            // Generate a small size cluster around the center
            for (int dr = FOOD_CLUSTER_MIN_OFFSET; dr <= FOOD_CLUSTER_MAX_OFFSET; dr++) {
                for (int dc = FOOD_CLUSTER_MIN_OFFSET; dc <= FOOD_CLUSTER_MAX_OFFSET; dc++) {
                    final CellIndex target = new CellIndex(center.row() + dr, center.column() + dc);

                    if (world.getGrid().isInside(target)) {
                        // the food has to not overwrite the nest (nest Clearence)
                        if (isInsideClearanceRadius(target, nestIndex, params.nestClearanceRadius())) {
                            continue;       // Skip for nest protection (avoiding food clusters very close to the nest)
                        }
                        if (world.getGrid().getCellAt(target).getCellContent() instanceof CellContent.Obstacle) {
                            continue;       // skip for not overwriting obstacles
                        }
                        world.getGrid().setCellContent(target, new CellContent.Food(params.foodQuantityPerCell()));
                    }
                }
            }
        }
    }

    /**
     * Checks if a targeted cell is within the nest clearance radius.
     *
     * @param target the target cell index
     * @param nest the nest cell index
     * @param radius the next clearance radius
     * @return true if the cell is inside the clearance radius
     */
    private boolean isInsideClearanceRadius(final CellIndex target, final CellIndex nest, final int radius) {
        return Math.abs(target.row() - nest.row()) <= radius && Math.abs(target.column() - nest.column()) <= radius;
    }
}
