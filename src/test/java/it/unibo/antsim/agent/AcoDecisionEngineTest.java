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
    void setUp(){
        world = new World(10, 10, 10.0, 10.0);
        pheromoneField = new PheromoneMap(10, 10, 10.0, 10.0, 100.0, new Evaporation(0.1));

        params = new AcoParameters(2.0, 1.0, 8.0, Math.PI/4, 0.0, Math.PI/16);
        engine = new AcoDecisionEngine(params, new Random(42));         // Fixed seed for deterministic behaviour;
    }

    @Test
    void testVerifyAcoDecisionEnginePrefersHighPheromones(){
        Ant ant = new Ant(new WorldPosition(55.0, 55.0), 0.0, 1.0);
        ant.setState(AntState.WANDERING);

        pheromoneField.deposit(new CellIndex(4, 6), PheromoneField.PheromoneType.FOOD, 90.0);

        int leftTurns = 0;
        int iteration = 100;
        for(int i=0; i<iteration; i++){
            double nextAngle = engine.decideNextAngle(ant, world, pheromoneField);
            if(nextAngle < ant.getAngle()){
                leftTurns++;
            }
        }
        assertTrue(leftTurns > 80, "The brains is not strong enough to make high pheromone preference. LeftTurns: "+leftTurns);
    }

    @Test
    void testTurnStrenghtLimitsTheAngleChange(){
        Ant ant = new Ant(new WorldPosition(55.0, 55.0), 0.0, 1.0);
        ant.setState(AntState.WANDERING);

        // Strong pheromone intensity on the left
        pheromoneField.deposit(new CellIndex(4, 6), PheromoneField.PheromoneType.FOOD, 90.0);

        double nextangle = engine.decideNextAngle(ant, world, pheromoneField);

        double appliedTurn = Math.abs(Math.atan2(Math.sin(nextangle - ant.getAngle()), Math.cos(nextangle - ant.getAngle())));

        assertTrue(appliedTurn <= params.turnStrength(), "The turn strength is not respected. Applied turn: "+appliedTurn);
        assertEquals(params.turnStrength(), appliedTurn, 0.001, "The turn strength is not respected. Applied turn: "+appliedTurn);
    }
}


