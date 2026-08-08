package it.unibo.antsim.world;

import java.util.Objects;

/**
 * This class represents a mutable cell of the grid that host a specific CellContent.
 */
public class Cell {
    private CellContent content;

    /**
     * Instantiates a new Cell.
     *
     * @param content the content
     */
    public Cell(final CellContent content) {
        this.content = Objects.requireNonNull(content);
    }

    /**
     * Get cell content.
     *
     * @return the cell content
     */
    public CellContent getCellContent() {
        return this.content;
    }

    /**
     * Set cell content.
     *
     * @param c it can be obstacle, food, nest or empty
     */
    public void setCellContent(final CellContent c) {
        this.content = Objects.requireNonNull(c);
    }

    /**
     * This method allows to consume a certain amount of food from the cell, if it contains food.
     * If the food is completely consumed, the cell content is set to Empty.
     *
     * @param amount amount of food
     */
    public void consumeFood(final int amount) {
        if (this.content instanceof CellContent.Food foodContent) {
            final CellContent.Food updatedFood = foodContent.consume(amount);
            if (updatedFood.isEmpty()) {
                this.content = new CellContent.Empty();
            } else {
                this.content = updatedFood;
            }
        }
    }
}
