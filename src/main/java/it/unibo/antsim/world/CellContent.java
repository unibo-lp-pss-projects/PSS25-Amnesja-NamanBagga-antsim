package it.unibo.antsim.world;

/**
 * Rappresents the content and the state of a cell in the grid of the environment.
 */
public sealed interface CellContent permits
    CellContent.Empty,
    CellContent.Obstacle,
    CellContent.Food,
    CellContent.Nest {

    /**
     * Empty cell where agents can walk.
     */
    record Empty() implements CellContent { }

    /**
     * Obstacle cell.
     */
    record Obstacle() implements CellContent { }

    /**
     * Food cell with a certain amount of food.
     *
     * @param quantity amount of food available
     */
    record Food(int quantity) implements CellContent {

        /**
         * Instantiates a new Food.
         *
         * @param quantity the quantity
         */
        public Food {
            if (quantity < 0) {
                throw new IllegalArgumentException("Food quantity must be non-negative");
            }
        }

        /**
         * Consume a part of the food.
         *
         * @param amount the amount to consume
         * @return the new updated Food istance
         */
        public Food consume(final int amount) {
            if (amount < 0) {
                throw new IllegalArgumentException("Amount to consume must be non-negative");
            }
            return new Food(Math.max(0, this.quantity - amount));
        }

        /**
         * Check if the food amount is empty.
         *
         * @return true of quantity is zero
         */
        public boolean isEmpty() {
            return quantity == 0;
        }
    }

    /**
     * Nest cell.
     */
    record Nest() implements CellContent { }
}
