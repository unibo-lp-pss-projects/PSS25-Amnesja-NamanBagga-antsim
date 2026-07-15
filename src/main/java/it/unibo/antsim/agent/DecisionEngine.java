package it.unibo.antsim.agent;

import it.unibo.antsim.pheromone.PheromoneField;
import it.unibo.antsim.world.World;

/**
 * This interface represents the decision-making mechanism for ants.
 * It calculates the future heading direction based on enviromental stimuli.
 */
public interface DecisionEngine {
    /**
    * Computes the optimal direction for the ant based on its current state, obstacles and the active pheromone fields
    * @param ant                The ant who is making decision
    * @param world              The world in which the ant is operating
    * @param pheromoneField     The active pheromone field containing trails
    * @return                   the next heading angle in radians
    */
    double decideNextAngle(Ant ant, World world, PheromoneField pheromoneField);
}
