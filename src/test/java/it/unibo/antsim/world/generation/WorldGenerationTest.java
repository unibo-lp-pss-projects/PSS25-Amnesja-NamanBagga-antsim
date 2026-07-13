package it.unibo.antsim.world.generation;

import it.unibo.antsim.world.CellContent;
import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.World;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class WorldGenerationTest {
    private final GenerationParameters params = new GenerationParameters(
            40, 40,                     // Grid dimension
            10.0, 10.0,                  // Cell Dimension
            0.5,                                  // Rock probability
            10,                                   // Rock cluster
            5,                                    // Food cluster
            100,                                  // Food quanity x cell
            4                                     // Nest clearance radius
    );

    @Test
    void testObstaclesNeverOverlapNestClearanceRadius() {
        WorldGenerator generator = new WorldGenerator(new Random());
        World world = generator.generate(params);

        CellIndex nestIndex = world.getNestIndex();
        assertNotNull(nestIndex);

        // Verify that any cell around the nest clearance radius contains food or obstacles
        for (int r = -params.nestClearanceRadius(); r <= params.nestClearanceRadius(); r++) {
            for (int c = -params.nestClearanceRadius(); c <= params.nestClearanceRadius(); c++) {
                CellIndex check = new CellIndex(nestIndex.row() + r, nestIndex.column() + c);

                if (world.getGrid().isInside(check)) {
                    CellContent content = world.getGrid().getCellAt(check).getCellContent();
                    if (check.equals(nestIndex)) {
                        assertInstanceOf(CellContent.Nest.class, content);
                    } else {
                        // All the area around must be empty
                        assertInstanceOf(CellContent.Empty.class, content, "The cell" + check + "in the nest radius is not empty!");
                    }
                }
            }
        }
    }

    @Test
    void testDeterministicOutputWithFixedSeed(){
        long seed = 42L;

        // Generation of 2 world with the same seed
        WorldGenerator gen1 = new WorldGenerator(new Random(seed));
        World world1 = gen1.generate(params);

        WorldGenerator gen2 = new WorldGenerator(new Random(seed));
        World world2 = gen2.generate(params);

        // Verify the grid
        assertEquals(world1.getRows(), world2.getRows());
        assertEquals(world1.getColumns(), world2.getColumns());
        assertEquals(world1.getNestIndex(), world2.getNestIndex());

        for(int r=0; r<world1.getRows(); r++){
            for(int c=0; c<world1.getColumns(); c++){
                CellIndex ci = new CellIndex(r, c);
                CellContent content1 = world1.getGrid().getCellAt(ci).getCellContent();
                CellContent content2 = world2.getGrid().getCellAt(ci).getCellContent();

                assertEquals(content1, content2, "Error at the cell"+ci);
            }
        }
    }
}

