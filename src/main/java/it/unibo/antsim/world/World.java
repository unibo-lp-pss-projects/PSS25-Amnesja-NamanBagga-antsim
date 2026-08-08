package it.unibo.antsim.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Main aggregate of the world module that bridges the discrete grid an continuous sppace.
 * Manages the lifecycle of the nest and spatial interaction between cells
 */
public class World {
    private final Grid grid;
    private final double cellWidth;
    private final double cellHeight;
    private CellIndex nestindex;

    /**
     * Instantiates a new World.
     *
     * @param rows the rows
     * @param cols the cols
     * @param cellWidth the cell width
     * @param cellHeight the cell height
     */
    public World(final int rows, final int cols, final double cellWidth, final double cellHeight) {
        if (cellWidth <= 0 || cellHeight <= 0) {
            throw new IllegalArgumentException("Cell dimensions must be positive");
        }

        this.grid = new Grid(rows, cols);
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.nestindex = null; // The nest will be positioned after
    }

    /**
     * Gets the total number of row in grid.
     *
     * @return row count
     */
    public int getRows() {
        return grid.getRows();
    }

    /**
     * Gets the total number of column in grid.
     *
     * @return the column count
     */
    public int getColumns() {
        return grid.getColumns();
    }

    /**
     * Gets the width of the world.
     *
     * @return the width
     */
    public double getWidth() {
        return grid.getColumns() * cellWidth;
    }

    /**
     * Gets the height of the world.
     *
     * @return the height
     */
    public double getHeight() {
        return grid.getRows() * cellHeight;
    }

    /**
     * Gets the discrete grid.
     *
     * @return the grid instance
     */
    public Grid getGrid() {
        return this.grid;
    }

    /**
     * Returns the index of the nest in the grid.
     *
     * @return the current nest index
     */
    public CellIndex getNestIndex() {
        return this.nestindex;
    }

    /**
     * Relocates nest after clearing the prev nest cell ant it sets
     * onto a new position.
     *
     * @param newIndex new nest cell index
     */
    public void relocateNest(final CellIndex newIndex) {
        if (!grid.isInside(newIndex)) {
            throw new IndexOutOfBoundsException("Nest index is outside the grid");
        }

        if (this.nestindex != null) {
            // Remove the old nest
            grid.setCellContent(this.nestindex, new CellContent.Empty());
        }
        this.nestindex = newIndex;
        grid.setCellContent(newIndex, new CellContent.Nest());
    }

    /**
     * Converts continuous world coordinates into a corresponding cell.
     *
     * @param pos world position
     * @return cell index
     */
    public CellIndex convertToCellIndex(final WorldPosition pos) {
        int row = (int) Math.floor(pos.y() / cellHeight);
        int col = (int) Math.floor(pos.x() / cellWidth);

        // Clamp the values to ensure they are within the grid bounds
        row = Math.clamp(row, 0, grid.getRows() - 1);
        col = Math.clamp(col, 0, grid.getColumns() - 1);

        return new CellIndex(row, col);
    }

    /**
     * Calculates valid neighbors cell that are not blocked by obstacles.
     *
     * @param index target cell indexs
     * @return a list of adjacent cells
     */
    public List<CellIndex> getWalkableNeighbors(final CellIndex index) {
        final List<CellIndex> neighbors = new ArrayList<>();
        if (!grid.isInside(index)) {
            return neighbors;
        }

        for (int dRow = -1; dRow <= 1; dRow++) {
            for (int dCol = -1; dCol <= 1; dCol++) {
                if (dRow == 0 && dCol == 0) {
                    continue; // Skip the current cell
                }
                final CellIndex neighborIndex = new CellIndex(index.row() + dRow, index.column() + dCol);
                if (grid.isInside(neighborIndex)) {
                    final CellContent content = grid.getCellAt(neighborIndex).getCellContent();
                    if (!(content instanceof CellContent.Obstacle)) {
                        neighbors.add(neighborIndex);
                    }
                }
            }
        }
        return neighbors;
    }

    /**
     * Verifies is a given position is blocked by an obstacle or world boundaries.
     *
     * @param pos the world position
     * @return true if blocked or out of bounds
     */
    public boolean isBlockedAt(final WorldPosition pos) {
        final int col = (int) Math.floor(pos.x() / cellWidth);
        final int row = (int) Math.floor(pos.y() / cellHeight);

        // In case is out of boundries, it will be considered as blocked
        if (row < 0 || row >= grid.getRows() || col < 0 || col >= grid.getColumns()) {
            return true;
        }

        return grid.getCellAt(new CellIndex(row, col)).getCellContent() instanceof CellContent.Obstacle;
    }

    /**
     * Verifies if food is at a specified world position.
     *
     * @param pos world position
     * @return true if the cell contains food
     */
    public boolean isFoodAt(final WorldPosition pos) {
        final CellIndex index = convertToCellIndex(pos);
        return grid.getCellAt(index).getCellContent() instanceof CellContent.Food;
    }

    /**
     * Consumes one unit of food from a target index.
     *
     * @param index cell index containing food
     * @return trie if food was consumed
     */
    public boolean consumeFood(final CellIndex index) {
        if (!grid.isInside(index)) {
            return false;
        }
        final Cell cell = grid.getCellAt(index);
        if (cell.getCellContent() instanceof CellContent.Food food && food.quantity() > 0) {
            cell.consumeFood(1);
            return true;
        }
        return false;
    }

    /**
     * Verifies if the nest is at a specified world position.
     *
     * @param pos the world position
     * @return true if cell contains nest
     */
    public boolean isNestAt(final WorldPosition pos) {
        final CellIndex index = convertToCellIndex(pos);
        return grid.getCellAt(index).getCellContent() instanceof CellContent.Nest;
    }

    /**
     * Searches for cell containing food near a coordinate.
     * First it search in the cell and only then on the surrounding area.
     *
     * @param pos worls position
     * @return an optional containing the near food cell
     */
    public Optional<CellIndex> findFoodCellNear(final WorldPosition pos) {
        final CellIndex center = convertToCellIndex(pos);

        // Check the cell where is the agent
        if (grid.isInside(center) && grid.getCellAt(center).getCellContent() instanceof CellContent.Food) {
            return Optional.of(center);
        }

        // Check the neighbors (radius = 1):
        for (int dRow = -1; dRow <= 1; dRow++) {
            for (int dCol = -1; dCol <= 1; dCol++) {
                if (dRow == 0 && dCol == 0) {
                    continue; // Skip the current cell
                }
                final CellIndex neighborIndex = new CellIndex(center.row() + dRow, center.column() + dCol);
                if (grid.isInside(neighborIndex) && grid.getCellAt(neighborIndex).getCellContent() instanceof CellContent.Food) {
                    return Optional.of(neighborIndex);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Places food at a specifies cell index.
     *
     * @param cellIndex tarhet cell index
     * @param amount amout to place
     */
    public void placeFood(final CellIndex cellIndex, final int amount) {
        if (!grid.isInside(cellIndex)) {
            throw new IndexOutOfBoundsException("Food position is outside the grid");
        }
        if (cellIndex.equals(nestindex)) {
            throw new IllegalStateException("You cannot place food on the nest");
        }
        grid.setCellContent(cellIndex, new CellContent.Food(amount));
    }

    /**
     * Places obstacle at a specified cell index.
     *
     * @param cellIndex target cell index
     */
    public void placeObstacle(final CellIndex cellIndex) {
        if (!grid.isInside(cellIndex)) {
            throw new IndexOutOfBoundsException("Obstacle position is outside the grid");
        }
        if (cellIndex.equals(nestindex)) {
            throw new IllegalStateException("You cannot place an obstacle on the nest");
        }
        grid.setCellContent(cellIndex, new CellContent.Obstacle());
    }

    /**
     * Clear the content od a cell.
     *
     * @param cellIndex target cell
     */
    public void clearCell(final CellIndex cellIndex) {
        if (!grid.isInside(cellIndex)) {
            throw new IndexOutOfBoundsException("Cell position is outside the grid");
        }
        grid.setCellContent(cellIndex, new CellContent.Empty());
        if (cellIndex.equals(nestindex)) {
            nestindex = null;
        }
    }
}
