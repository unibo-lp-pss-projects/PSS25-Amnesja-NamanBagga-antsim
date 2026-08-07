package it.unibo.antsim.view;

import it.unibo.antsim.world.CellIndex;

public record WorldEdit(CellIndex cellIndex, EditorTool tool, int foodAmount) {
}
