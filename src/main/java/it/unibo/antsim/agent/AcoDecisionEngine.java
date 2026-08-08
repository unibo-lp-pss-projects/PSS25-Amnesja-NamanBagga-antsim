package it.unibo.antsim.agent;

import it.unibo.antsim.pheromone.PheromoneField;
import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.World;
import it.unibo.antsim.world.WorldPosition;

import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Implementation of the decision engine using ACO (Ant Colony Optimization) algorithm.
 * It uses 3 front sensors (left, center, right) to sample the pheromone levels
 * and selects the next heading angle using a probabilistic roulette-wheel.
 */
public class AcoDecisionEngine implements DecisionEngine {
    private static final double EPSILON = 0.01;      // Small value to allow movement in empty fields
    private static final double HOMING_WEIGHT = 1.5;
    private final AcoParameters params;
    private final RandomGenerator random;

    /**
     * Instantiates a new Aco decision engine.
     *
     * @param params the parameters
     * @param random the random generator for tests
     */
    public AcoDecisionEngine(final AcoParameters params, final RandomGenerator random) {
        this.params = Objects.requireNonNull(params, "ACO parameters cannot be null");
        this.random = Objects.requireNonNull(random, "Random generator cannot be null");
    }

    /**
     * Decides the next movement (heading angle) for an ant based on its current state.
     *
     * @param ant The ant who is making decision
     * @param world The world in which the ant is operating
     * @param pheromoneField The active pheromone field containing trails
     * @return the calculated heading angle in radians
     */
    @Override
    public double decideNextAngle(final Ant ant, final World world, final PheromoneField pheromoneField) {
        Objects.requireNonNull(ant, "Ant cannot be null");
        Objects.requireNonNull(world, "World cannot be null");
        Objects.requireNonNull(pheromoneField, "Pheromone field cannot be null");

        // Determine which pheromone type the ant is sensitive in current state
        final PheromoneField.PheromoneType target = ant.getState() == AntState.RETURNING_TO_NEST
                ? PheromoneField.PheromoneType.HOME : PheromoneField.PheromoneType.FOOD;

        // Define 3 candidates of the current angle
        final double currentAngle = ant.getAngle();
        final double[] candidates = {
                currentAngle,                                   // Center
                currentAngle - params.sensorAngle(),            // Left
                currentAngle + params.sensorAngle(),            // Right
        };

        final double[] weights = new double[3];
        double totalweight = 0.0;

        final boolean explorer = ant.getRole() == AntRole.EXPLORER;

        final double explorationProb = explorer ? 0.15 : 0.05;
        final double explorationRate = explorer ? 0.03 : 0.01;
        final double noiseFactor = explorer ? params.randomFactor() : params.randomFactor() * 0.25;

        double homingAngle = Double.NaN;
        if (ant.getState() == AntState.RETURNING_TO_NEST) {
            final CellIndex nestIndex = world.getNestIndex();
            if (nestIndex != null) {
                final double cellWidth = world.getWidth() / world.getColumns();
                final double cellHeight = world.getHeight() / world.getRows();
                final double nestX = (nestIndex.column() + 0.5) * cellWidth;
                final double nestY = (nestIndex.row() + 0.5) * cellHeight;
                homingAngle = Math.atan2(nestY - ant.getPosition().y(), nestX - ant.getPosition().x());
            }
        }
        for (int i = 0; i < 3; i++) {
            final double angle = candidates[i];

            // Calculates sensor position in world coordinates
            final double sensorX = ant.getPosition().x() + params.sensorRange() * Math.cos(angle);
            final double sensorY = ant.getPosition().y() + params.sensorRange() * Math.sin(angle);
            final WorldPosition pos = new WorldPosition(sensorX, sensorY);

            // Avoid choosing directions that lead straight into obstacles
            if (world.isBlockedAt(pos)) {
                weights[i] = 0.0;
                continue;
            }

            // Sample pheromone level and calculate attraction weight
            final double pheromone = pheromoneField.level(pos, target);

            // Traditional Aco formula: weight = (pheromone + epsilon) ^ alpha
            final double pheromoneWeight = Math.pow(pheromone + EPSILON, params.alpha());

            // Directional heuristic weight, ants prefer to move forward (higher weight in the center)
            final double heuristic = (i == 0) ? 1.0 : 0.5;
            final double heuristicWeight = Math.pow(heuristic, params.beta());

            weights[i] = (1.0 - explorationRate) * pheromoneWeight * heuristicWeight + explorationRate;
            if (!Double.isNaN(homingAngle)) {
                final double alignment = Math.cos(homingAngle - angle);
                weights[i] *= Math.exp(HOMING_WEIGHT * alignment);
            }
            totalweight += weights[i];
        }

        // If all paths are blocked or the weight is zero, make a random choice to avoid deadlock
        if (totalweight <= 0.0) {
            final double targetAngle = currentAngle + (random.nextDouble() - 0.5) * params.randomFactor();
            return applyTurnStrength(currentAngle, targetAngle);
        }

        // Exploration trigger
        if (random.nextDouble() < explorationProb) {
            for (int attempt = 0; attempt < candidates.length; attempt++) {
                final int candidate = random.nextInt(candidates.length);

                if (weights[candidate] > 0.0) {
                    final double noise = (random.nextDouble() - 0.5) * noiseFactor;
                    return applyTurnStrength(currentAngle, candidates[candidate] + noise);
                }
            }
        }

        // Roulette wheel selection to choose on of the tree directions
        final double roll = random.nextDouble() * totalweight;
        double cumulative = 0.0;
        for (int i = 0; i < 3; i++) {
            cumulative += weights[i];
            if (roll <= cumulative) {
                // Add a small random noise to the chosen angle to make look the movement organic
                final double noise = (random.nextDouble() - 0.5) * noiseFactor;
                final double targetAngle = candidates[i] + noise;
                return applyTurnStrength(currentAngle, targetAngle);
            }
        }
        return currentAngle;
    }

    /**
     * this method is used for a natural and controlled turning movement.
     *
     * @param currentAngle the current orientation of an ant
     * @param targetAngle the desired target orientation in radians
     * @return the new angle in radians after applying maximum turn limit
     */
    private double applyTurnStrength(final double currentAngle, final double targetAngle) {
        final double difference = Math.atan2(Math.sin(targetAngle - currentAngle), Math.cos(targetAngle - currentAngle));
        final double limitedDifference = Math.clamp(difference, -params.turnStrength(), params.turnStrength());

        return currentAngle + limitedDifference;
    }
}
