package it.unibo.antsim.pheromone;
/*
* This interface is basically a strategy for pheromone evaporation
*/
public interface EvaporationModel {
    // It calculates the new level of pheromones based on the current level
    double decay(double currentLevel, double dt);
}
