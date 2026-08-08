package it.unibo.antsim.pheromone;

import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.WorldPosition;

/**
 * The interface Pheromone Field.
 */
public interface PheromoneField {
    /**
     * Types of pheromones.
     */
    enum PheromoneType {
        FOOD, HOME
    }

    /**
     * This method returns the level of a specific pheromone type at a given position.
     *
     * @param pos the position in the world
     * @param type type of pheromone
     * @return level of the pheromone
     */
    double level(WorldPosition pos, PheromoneType type);

    /**
     * This method manages the deposition of pheromones by given an intensity.
     *
     * @param index cell index
     * @param type pheromone type
     * @param intensity intensity of the pheromone
     */
    void deposit(CellIndex index, PheromoneType type, double intensity);

    /**
     * This method manages the evaporation of pheromones.
     *
     * @param dt time value
     */
    void evaporate(double dt);

    /**
     * Clear method.
     */
    void clear();
}
