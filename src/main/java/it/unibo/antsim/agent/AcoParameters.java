package it.unibo.antsim.agent;

/**
 * This record is for configuration parameters for the ACO (Ant Colony Optimization) algorithm.
 *
 * @param alpha weight associated with the pheromone intensity
 * @param beta weight associated with the heuristic information
 * @param sensorRange sensory range of an ant within which it detects environmental stimuli
 * @param sensorAngle width of the frontal sensory cone in radians
 * @param randomFactor noise factor injected into decision to encourage exploration
 * @param turnStrength maximum angle that an ant can rotate per step
 */
public record AcoParameters(
        double alpha,
        double beta,
        double sensorRange,
        double sensorAngle,
        double randomFactor,
        double turnStrength
) {
    /**
     * Instantiates a new Aco parameters.
     *
     * @param alpha the alpha
     * @param beta the beta
     * @param sensorRange the sensor range
     * @param sensorAngle the sensor angle
     * @param randomFactor the random factor
     * @param turnStrength the turn strength
     */
    public AcoParameters {
        if (alpha < 0 || beta < 0 || sensorRange <= 0 || sensorAngle <= 0 || randomFactor < 0 || turnStrength <= 0) {
            throw new IllegalArgumentException("Invalid ACO parameters must be non-negative.");
        }
    }
}
