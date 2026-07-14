package it.unibo.antsim.agent;

/*
* behaviour of ants agent
*/
public enum AntState {
    // the ants explore the world for finding some food (HOME type pheromone deposition)
    WANDERING,

    // The ants have found the food and ther're heading to it
    TARGETING_FOOD,

    // The ants carry the food to the nest (FOOD type pheromone deposition)
    RETURNING_TO_NEST
}
