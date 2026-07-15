package it.unibo.antsim.agent;
/*
* This record is for configuration parameters for the ACO (Ant Colony Optimization) algorithm
*/
public record AcoParameters(
        double alpha,                       // Weight associated with pheromone intensity
        double beta,                        // Weight associated with heuristic information
        double sensorRange,                 // Sensory range of an ant within which it detects environmental stimuli
        double sensorAngle,                 // Width of the frontal sensory cone in radians
        double randomFactor,                // Noise factor injected into decision to encourage exploration
        double turnStrength                // Maximum angle that an ant can rotate to per step
) {
    public AcoParameters{
        if(alpha<0 || beta <0 || sensorRange<=0 || sensorAngle<=0 || randomFactor<0 || turnStrength<=0){
            throw new IllegalArgumentException("Invalid ACO parameters: alpha, beta, sensorRange, sensorAngle, and randomFactor must be non-negative, and sensorRange and sensorAngle must be positive.");
        }
    }
}
