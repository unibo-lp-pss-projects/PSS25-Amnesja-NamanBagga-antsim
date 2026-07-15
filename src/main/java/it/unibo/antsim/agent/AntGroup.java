package it.unibo.antsim.agent;

import it.unibo.antsim.pheromone.PheromoneField;
import it.unibo.antsim.world.World;

import java.util.*;

/**
 * This class manages a collection of ants, coordinating their decision and movement
 */
public class AntGroup {
    private final List<Ant> ants;

    // Creates an empty ant group
    public AntGroup() {
        this.ants = new ArrayList<>();
    }

    // Adds an ant in the group
    public void addAnt(Ant ant){
        this.ants.add(Objects.requireNonNull(ant));
    }

    // Returns an unmodifiable list of ants in the group
    public List<Ant> getAnts() {
        return Collections.unmodifiableList(ants);
    }

    /**
     * This method updates all ants in the group by executing their decisiona and movement
     * @param dt                                Time delta for the physics update step
     * @param world                             The world in which the ants are operating
     * @param pheromoneField                    The active pheromone field containing trails
     * @param engine                            The decision engine used by the ants to determine their next heading
     */
    public void update(double dt, World world, PheromoneField pheromoneField, DecisionEngine engine){
        Objects.requireNonNull(world, "World cannot be null");
        Objects.requireNonNull(pheromoneField, "PheromoneField cannot be null");
        Objects.requireNonNull(engine, "DecisionEngine cannot be null");

        for(Ant ant: ants){
            // Brain decides the new heading
            double nextAngle = engine.decideNextAngle(ant, world, pheromoneField);
            ant.setAngle(nextAngle);

            // Ant moves in the world
            ant.move(dt, world);
        }
    }
}
