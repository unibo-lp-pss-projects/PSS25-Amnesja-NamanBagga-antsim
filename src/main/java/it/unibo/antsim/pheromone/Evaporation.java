package it.unibo.antsim.pheromone;

/**
 * The type Evaporation.
 *
 * @param decayRate evaporation rate
 */
public record Evaporation(double decayRate) implements EvaporationModel {

    /**
     * Instantiates a new Evaporation.
     *
     * @param decayRate the decay rate
     */
    public Evaporation {
        if (decayRate < 0) {
            throw new IllegalArgumentException("Decay rate must be between 0 and 1");
        }
    }

    /**
     * Exponential decay algorithm.
     *
     * @param currentLevel the current level
     * @param dt           the dt
     * @return new decayed level of pheromone level
     */
    @Override
    public double decay(final double currentLevel, final double dt) {
        if (dt < 0) {
            throw new IllegalArgumentException("Time delta must be positive!");
        }
        return currentLevel * Math.exp(-decayRate * dt);
    }
}
