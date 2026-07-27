package it.unibo.antsim.simulation;

import it.unibo.antsim.agent.AcoDecisionEngine;
import it.unibo.antsim.agent.AcoParameters;
import it.unibo.antsim.agent.AntFactory;
import it.unibo.antsim.pheromone.Evaporation;
import it.unibo.antsim.pheromone.PheromoneMap;
import it.unibo.antsim.world.World;
import it.unibo.antsim.world.generation.GenerationParameters;
import it.unibo.antsim.world.generation.WorldGenerator;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimulationConvergenceTest {
    private static final long SEED = 42L;
    private static final int MAX_STEPS = 500;
    private static final int STABILITY_WINDOW = 100;

    @Test
    void sameSeedProducesIdenticalSimulationOutcome(){
        GenerationParameters params = new GenerationParameters(10, 10, 10.0, 10.0, 0.0, 0, 0, 10, 1);
        SimulationEngine first = buildEngine(new Random(SEED), params);
        SimulationEngine second = buildEngine(new Random(SEED), params);

        first.start();
        second.start();

        for(int i=0; i<MAX_STEPS; i++){
            first.step(1.0);
            second.step(1.0);
        }

        assertEquals(first.getStats().foodCollected(), second.getStats().foodCollected(), "Simulations with same seed must collect same quantiity of food");
        assertEquals(first.getClock().getCurrentStep(), second.getClock().getCurrentStep());
    }

    @Test
    void simulationReachesSteadyStateBeforeMaxSteps(){
        GenerationParameters params = new GenerationParameters(10, 10, 10.0, 10.0, 0.0, 0, 0, 10, 1);
        SimulationEngine engine = buildEngine(new Random(SEED), params);
        engine.start();

        int lastStable = -1;
        int unchangedRuns = 0;

        for(int i=0; i<=MAX_STEPS; i++){
            engine.step(1.0);
            int collected = engine.getStats().foodCollected();

            if(collected==lastStable){
                unchangedRuns++;
                if(unchangedRuns>=STABILITY_WINDOW){
                    assertEquals(collected, engine.getStats().foodCollected(), "Food collected must remain stable once converged");
                    return;
                }
            }else{
                unchangedRuns = 0;
                lastStable = collected;
            }
        }

        assertEquals(lastStable, engine.getStats().foodCollected(), "Food collected must not decrease at simulation end");
    }
    private SimulationEngine buildEngine(Random seed, GenerationParameters params){
        WorldGenerator gen  =new WorldGenerator(seed);
        World world = gen.generate(params);
        PheromoneMap pheromoneMap = new PheromoneMap(params.rows(), params.cols(), params.cellWidth(), params.cellHeight(), 100.0, new Evaporation(1.0));
        AcoParameters acoParams = new AcoParameters(1.0, 1.0, 5.0, Math.PI/4, 0.0, 1.0);
        AcoDecisionEngine decisionEngine = new AcoDecisionEngine(acoParams, seed);
        AntFactory antFactory = new AntFactory(1.0, seed);
        return new SimulationEngine(world, pheromoneMap, decisionEngine, antFactory, gen, params );
    }
}
