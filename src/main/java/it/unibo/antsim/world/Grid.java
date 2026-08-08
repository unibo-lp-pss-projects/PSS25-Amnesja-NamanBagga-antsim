package it.unibo.antsim.world;

import java.util.Objects;

/**
 * Manages the two-dimensional grid securing the edge controls.
 */
public class Grid {
    private final int rows;
    private final int columns;
    private final Cell[][] cells;

    /**
     * Instantiates a new Grid.
     *
     * @param rows the rows
     * @param columns the columns
     */
    public Grid(final int rows, final int columns) {
        if (rows <= 0 || columns <= 0) {
            throw new IllegalArgumentException("Grid dimensions must be positive");
        }
        this.rows = rows;
        this.columns = columns;
        this.cells = new Cell[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                cells[i][j] = new Cell(new CellContent.Empty());
            }
        }
    }

    /**
     * Getter for the rows.
     *
     * @return the number of rows
     */
    public int getRows() {
        return this.rows;
    }

    /**
     * Getter for the columns.
     *
     * @return the number of columns
     */
    public int getColumns() {
        return this.columns;
    }

    /**
     * Verifies if a cell index is inside the grid boundaries.
     *
     * @param index cell index to check
     * @return true if the index is inside the gride
     */
    public boolean isInside(final CellIndex index) {
        return index.row() >= 0 && index.row() < rows && index.column() >= 0 && index.column() < columns;
    }

    /**
     * Returns the cell at the specified index.
     *
     * @param index the target index
     * @return tge cell at the index
     */
    public Cell getCellAt(final CellIndex index) {
        if (!isInside(index)) {
            throw new IndexOutOfBoundsException("Index is outside the grid");
        }
        return cells[index.row()][index.column()];
    }

    /**
     * Modifies the cell content at the specified index.
     *
     * @param index the target index
     * @param content the new content
     */
    public void setCellContent(final CellIndex index, final CellContent content) {
        Objects.requireNonNull(content);
        getCellAt(index).setCellContent(content);
    }
}
