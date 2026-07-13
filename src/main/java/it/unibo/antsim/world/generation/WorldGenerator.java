package it.unibo.antsim.world.generation;

import it.unibo.antsim.world.CellContent;
import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.World;

import java.util.random.RandomGenerator;

/*
* This class manages the world generation for the simulation
*/
public class WorldGenerator {
    private final RandomGenerator random;

    public WorldGenerator(RandomGenerator random) {
        this.random = random;       // Initialize the random source
    }

    public World generate(GenerationParameters params){
        World world = new World(params.rows(), params.cols(), params.cellWidth(), params.cellHeight());

        // nest position at the center of the world as a default
        CellIndex nestIndex = new CellIndex(params.rows()/2, params.cols()/2);
        world.relocateNest(nestIndex);

        // Generation of the obstacles cluster
        generateRockClusters(world, nestIndex, params);

        // Generation of the food cluster
        generateFoodClusters(world, nestIndex, params);

        return world;
    }

    private void generateRockClusters(World world, CellIndex nestIndex, GenerationParameters params){
        for(int i=0; i<params.numRockClusters(); i++){
            // Initial starting random for the cluster
            int startRow = random.nextInt(params.rows());
            int startCol = random.nextInt(params.cols());
            CellIndex center = new CellIndex(startRow, startCol);

            // Expansion of the cluster around at the center
            for(int dr=-2; dr<= 2; dr++){
                for(int dc=-2; dc<= 2; dc++){
                    CellIndex target = new CellIndex(center.row()+dr, center.column()+dc);

                    if(world.getGrid().isInside(target)){
                        // Verifiy id the cell is near the nest
                        if(isInsideClearanceRadius(target, nestIndex, params.nestClearanceRadius())){
                            continue;       // Skip for nest protection (avoiding obstacles very close to the nest)
                        }

                        // Probability of rock generation decreasing with how far is from the center
                        double distanceModifier = 1.0/(1.0+Math.hypot(dr, dc));
                        if(random.nextDouble() < (params.rockProbability() * distanceModifier)){
                            world.getGrid().setCellContent(target, new CellContent.Obstacle());
                        }
                    }
                }
            }
        }
    }

    private void generateFoodClusters(World world, CellIndex nestIndex, GenerationParameters params) {
        for (int i = 0; i <= params.numFoodClusters(); i++) {
            int startRow = random.nextInt(params.rows());
            int startCol = random.nextInt(params.cols());
            CellIndex center = new CellIndex(startRow, startCol);

            // Generate a small size cluster around the center
            for (int dr = 0; dr <= 1; dr++) {
                for (int dc = 0; dc <= 1; dc++) {
                    CellIndex target = new CellIndex(center.row() + dr, center.column() + dc);

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

    private boolean isInsideClearanceRadius(CellIndex target, CellIndex nest, int radius){
        return Math.abs(target.row() - nest.row()) <= radius && Math.abs(target.column() - nest.column()) <= radius;
    }
}
