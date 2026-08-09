package it.unibo.antsim.view;

import it.unibo.antsim.world.CellIndex;

/**
 * The type World edit.
 *
 * @param cellIndex cell index
 * @param tool tool used
 * @param foodAmount amount of food
 */
public record WorldEdit(CellIndex cellIndex, EditorTool tool, int foodAmount) {
}
