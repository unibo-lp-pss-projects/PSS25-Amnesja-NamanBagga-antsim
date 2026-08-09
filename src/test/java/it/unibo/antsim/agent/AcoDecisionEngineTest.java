package it.unibo.antsim.agent;

import it.unibo.antsim.pheromone.Evaporation;
import it.unibo.antsim.pheromone.PheromoneField;
import it.unibo.antsim.pheromone.PheromoneMap;
import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.World;
import it.unibo.antsim.world.WorldPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AcoDecisionEngineTest {
    private World world;
    private PheromoneMap pheromoneField;
    private AcoDecisionEngine engine;
    private AcoParameters params;

    @BeforeEach
    void setUp() {
        final double decayRate = 0.1;
        final long seed = 42L;
        world = new World(10, 10, 10.0, 10.0);
        pheromoneField = new PheromoneMap(10, 10, 10.0, 10.0, 100.0, new Evaporation(decayRate));

        params = new AcoParameters(2.0, 1.0, 8.0, Math.PI / 4, 0.0, Math.PI / 16);
        engine = new AcoDecisionEngine(params, new Random(seed));         // Fixed seed for deterministic behaviour;
    }

    @Test
    void testVerifyAcoDecisionEnginePrefersHighPheromones() {
        final Ant ant = new Ant(new WorldPosition(55.0, 55.0), 0.0, 1.0, AntRole.FOLLOWER);
        ant.setState(AntState.WANDERING);

        final int r = 4;
        final int c = 6;
        pheromoneField.deposit(new CellIndex(r, c), PheromoneField.PheromoneType.FOOD, 90.0);

        int leftTurns = 0;
        for (int i = 0; i < 100; i++) {
            final double nextAngle = engine.decideNextAngle(ant, world, pheromoneField);
            if (nextAngle < ant.getAngle()) {
                leftTurns++;
            }
        }

        final int conditionIterationValue = 80;
        assertTrue(leftTurns > conditionIterationValue,
                "The brains is not strong enough to make high pheromone preference. LeftTurns: " + leftTurns);
    }

    @Test
    void testTurnStrengthLimitsTheAngleChange() {
        final Ant ant = new Ant(new WorldPosition(55.0, 55.0), 0.0, 1.0, AntRole.FOLLOWER);
        ant.setState(AntState.WANDERING);

        // Strong pheromone intensity on the left
        final int r = 4;
        final int c = 6;
        pheromoneField.deposit(new CellIndex(r, c), PheromoneField.PheromoneType.FOOD, 90.0);

        final double nextAngle = engine.decideNextAngle(ant, world, pheromoneField);

        final double appliedTurn = Math.abs(
                Math.atan2(
                        Math.sin(nextAngle - ant.getAngle()),
                        Math.cos(nextAngle - ant.getAngle())
                )
        );
        final double delta = 0.001;
        assertTrue(appliedTurn <= params.turnStrength(),
                "The turn strength is not respected. Applied turn: " + appliedTurn);
        assertEquals(params.turnStrength(), appliedTurn, delta,
                "The turn strength is not respected. Applied turn: " + appliedTurn);
    }
}

