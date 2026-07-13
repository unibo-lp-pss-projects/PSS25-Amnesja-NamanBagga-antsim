package it.unibo.antsim.world.generation;

/*
* This record contains the configuration parameters for world generation
*/
public record GenerationParameters(
        int rows,
        int cols,
        double cellWidth,
        double cellHeight,
        double rockProbability,     // Base probability of rock (obstacle) generation
        int numRockClusters,        // Numbers of rock cluster to generate
        int numFoodClusters,        // Number of food cluster to generate
        int foodQuantityPerCell,    // Initial quanitity of food per cell
        int nestClearanceRadius     // Radius of cell around the nest that have to be totaly free from obstacles
) {
    public GenerationParameters {
        if(rows<=0 || cols<=0 || cellWidth <=0 || cellHeight <=0){
            throw new IllegalArgumentException("Invalid generation parameters");
        }
        if(rockProbability < 0 || rockProbability > 1){
            throw new IllegalArgumentException("Invalid rock probability");
        }
    }
}
