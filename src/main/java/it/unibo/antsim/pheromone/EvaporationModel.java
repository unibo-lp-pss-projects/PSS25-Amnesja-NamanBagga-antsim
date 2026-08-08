package it.unibo.antsim.pheromone;

/**
 * The interface Evaporation model.
 */
public interface EvaporationModel {
    /**
     * It calculates the new level of pheromones based on the current level.
     *
     * @param currentLevel the current level
     * @param dt           the dt
     * @return the double
     */
    double decay(double currentLevel, double dt);
}
