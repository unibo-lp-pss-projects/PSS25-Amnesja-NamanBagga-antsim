package it.unibo.antsim.world.generation;

/**
 * This record contains the configuration parameters for world generation.
 *
 * @param rows generation map row
 * @param cols generation map columns
 * @param cellWidth width length for each cell
 * @param cellHeight height length for each cell
 * @param rockProbability base probability of obstacle generation
 * @param numRockClusters numbers of rock cluster to generate
 * @param numFoodClusters numbers of food cluster to generate
 * @param foodQuantityPerCell amount of food per cell
 * @param nestClearanceRadius radius od cell around the nest to be totally free from obstacles generation
 */
public record GenerationParameters(
        int rows,
        int cols,
        double cellWidth,
        double cellHeight,
        double rockProbability,
        int numRockClusters,
        int numFoodClusters,
        int foodQuantityPerCell,
        int nestClearanceRadius
) {

    /**
     * Instantiates a new Generation parameters.
     *
     * @param rows the rows
     * @param cols the cols
     * @param cellWidth the cell width
     * @param cellHeight the cell height
     * @param rockProbability the rock probability
     * @param numRockClusters the num rock clusters
     * @param numFoodClusters the num food clusters
     * @param foodQuantityPerCell the food quantity per cell
     * @param nestClearanceRadius the nest clearance radius
     */
    public GenerationParameters {
        if (rows <= 0 || cols <= 0 || cellWidth <= 0 || cellHeight <= 0) {
            throw new IllegalArgumentException("Invalid generation parameters");
        }
        if (rockProbability < 0 || rockProbability > 1) {
            throw new IllegalArgumentException("Invalid rock probability");
        }
    }
}
