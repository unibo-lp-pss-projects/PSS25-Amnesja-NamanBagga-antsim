package it.unibo.antsim.pheromone;

import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.WorldPosition;

public interface PheromoneField {
    // Types of pheromones
    enum PheromoneType {
        FOOD, HOME
    }

    /*
    * This method returns the level of a specific pheromone type at a given position
    */
    double level(WorldPosition pos, PheromoneType type);

    /*
    * This method manages the deposition of pheromones by given an intensity
    */
    void deposit(CellIndex index, PheromoneType type, double intensity);

    /*
    * This method manages the evaporation of pheromones
    */
    void evaporate(double dt);
}
