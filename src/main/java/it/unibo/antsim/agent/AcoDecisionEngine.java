package it.unibo.antsim.agent;

import it.unibo.antsim.pheromone.PheromoneField;
import it.unibo.antsim.world.World;
import it.unibo.antsim.world.WorldPosition;

import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Implementation of the decision engine using ACO (Ant Colony Optimization) algorithm.
 * It uses 3 front sensors (left, center, right) to sample the pheromone levels
 * and selects the next heading angle using a proabilistic roulette-wheel
 */
public class AcoDecisionEngine implements DecisionEngine {
    private final AcoParameters params;
    private final RandomGenerator random;
    private static final double EPSILON = 0.01;      // Small value to allow movement in empty fields
    private static final double EXPLORATION_RATE = 0.10;

    public AcoDecisionEngine(AcoParameters params, RandomGenerator random) {
        this.params = Objects.requireNonNull(params, "ACO parameters cannot be null");
        this.random = Objects.requireNonNull(random, "Random generator cannot be null");
    }

    @Override
    public double decideNextAngle(Ant ant, World world, PheromoneField pheromoneField){
        Objects.requireNonNull(ant, "Ant cannot be null");
        Objects.requireNonNull(world, "World cannot be null");
        Objects.requireNonNull(pheromoneField, "Pheromone field cannot be null");

        // Determine which pheromone type the ant is sensitive in current state
        PheromoneField.PheromoneType target = PheromoneField.PheromoneType.FOOD;

        // Define 3 candidates of the current angole
        double currentAngle = ant.getAngle();
        double[] candidates = {
                currentAngle,                                   // Center
                currentAngle - params.sensorAngle(),            // Left
                currentAngle + params.sensorAngle()             // Right
        };

        double[] weights = new double[3];
        double totalweight = 0.0;

        double explorationProb = ant.getRole() == AntRole.EXPLORER ? 0.35 : 0.05;
        double noiseFactor = ant.getRole() == AntRole.EXPLORER ? params.randomFactor() : params.randomFactor() * 0.25;

        for(int i=0; i<3; i++){
            double angle = candidates[i];

            // Sensor position in space
            double sensorX = ant.getPosition().x() + params.sensorRange() * Math.cos(angle);
            double sensorY = ant.getPosition().y() + params.sensorRange() * Math.sin(angle);
            WorldPosition pos = new WorldPosition(sensorX, sensorY);
            // Avoid choosing directions that lead straight into obstacles
            if(world.isBlockedAt(pos)){
                weights[i] = 0.0;
                continue;
            }

            // Sample pheromone level and calculate attraction weight
            double pheromone = pheromoneField.level(pos, target);

            // Traditional Aco formula: weight = (pheromone + epsilon) ^ alpha
            double pheromoneWeight = Math.pow(pheromone + EPSILON, params.alpha());

            // Directional heuristic weight, ants prefer to move forward (higher weight in the center)
            double heuristic = (i==0) ? 1.0 : 0.5;
            double heuristicWeight = Math.pow(heuristic, params.beta());

            weights[i] = (1.0 - EXPLORATION_RATE) * pheromoneWeight * heuristicWeight + EXPLORATION_RATE;
            totalweight += weights[i];
        }
        // If all paths are blocked or the weight is zero, make a random choice to avoid deadlock
        if(totalweight <= 0.0){
            double targetAngle = currentAngle + (random.nextDouble() - 0.5) * params.randomFactor();
            return applyTurnStrength(currentAngle, targetAngle);
        }

        if(random.nextDouble() < explorationProb){
            for(int attempt=0; attempt<candidates.length; attempt++){
                int candidate = random.nextInt(candidates.length);

                if(weights[candidate] > 0.0){
                    double noise = (random.nextDouble() - 0.5) * noiseFactor;
                    return applyTurnStrength(currentAngle, candidates[candidate] + noise);
                }
            }
        }

        // Roulette wheel selection to choose on of the tree directions
        double roll = random.nextDouble() * totalweight;
        double cumulative = 0.0;
        for(int i=0; i<3; i++){
            cumulative += weights[i];
            if(roll<=cumulative){
                // Add a small random noise to the chosen angle to make look the movement organic
                double noise = (random.nextDouble() - 0.5) * noiseFactor;
                double targetAngle =  candidates[i] + noise;
                return applyTurnStrength(currentAngle, targetAngle);
            }
        }
        return currentAngle;
    }

    private double applyTurnStrength(double currentAngle, double targetAngle){
        double difference = Math.atan2(Math.sin(targetAngle - currentAngle), Math.cos(targetAngle - currentAngle));
        double limitedDifference = Math.clamp(difference, -params.turnStrength(), params.turnStrength());

        return currentAngle + limitedDifference;
    }
}
