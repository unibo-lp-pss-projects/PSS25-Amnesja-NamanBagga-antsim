package it.unibo.antsim.simulation;

import it.unibo.antsim.agent.AcoDecisionEngine;
import it.unibo.antsim.agent.AcoParameters;
import it.unibo.antsim.agent.Ant;
import it.unibo.antsim.agent.AntFactory;
import it.unibo.antsim.pheromone.Evaporation;
import it.unibo.antsim.pheromone.PheromoneField;
import it.unibo.antsim.pheromone.PheromoneMap;
import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.World;
import it.unibo.antsim.world.WorldPosition;
import it.unibo.antsim.world.generation.GenerationParameters;
import it.unibo.antsim.world.generation.WorldGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class SimulationStepTest {
    private SimulationEngine engine;
    private PheromoneMap pheromoneMap;

    @BeforeEach
    void setUp(){
        World world = new World(10, 10, 10.0, 10.0);
        pheromoneMap = new PheromoneMap(10, 10, 10.0, 10.0, 100.0, new Evaporation(1.0));
        AcoParameters params = new AcoParameters(1.0, 1.0, 5.0, Math.PI/4, 0.0, 1.0);
        AcoDecisionEngine decisionEngine = new AcoDecisionEngine(params, new Random(42));
        engine = new SimulationEngine(world, pheromoneMap, decisionEngine, new AntFactory(1.0, new Random(42)),new WorldGenerator(new Random(42)),
                new GenerationParameters(10, 10, 10.0, 10.0, 0.0, 0, 0, 10, 1));
    }

    @Test
    void testStepAdvancesClockAndMoveAgents(){
        Ant ant = new Ant(new WorldPosition(15.0, 15.0), 0.0, 2.0);
        engine.addAnt(ant);
        engine.start();

        WorldPosition initialPos = ant.getPosition();
        engine.step(1.0);

        // Verify clock ticked
        assertEquals(1, engine.getClock().getCurrentStep());
        assertEquals(1.0, engine.getClock().getTotalTime(), 0.001);

        // Verify ant moved forward
        assertNotEquals(initialPos, ant.getPosition());
        assertTrue(ant.getPosition().x() > initialPos.x());
    }

    @Test
    void testStepTriggersPheromoneEvaporation(){
        CellIndex cell = new CellIndex(1, 1);
        pheromoneMap.deposit(cell, PheromoneField.PheromoneType.FOOD, 10.0);
        engine.start();

        engine.step(2.0);   // 2 sec * 1.0 rate = 2.0 units per second

        double level = pheromoneMap.level(new WorldPosition(15, 15), PheromoneField.PheromoneType.FOOD);
        assertEquals(8.0, level, 0.001);
    }
}
