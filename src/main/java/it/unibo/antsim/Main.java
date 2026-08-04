package it.unibo.antsim;

import it.unibo.antsim.agent.AcoDecisionEngine;
import it.unibo.antsim.agent.AcoParameters;
import it.unibo.antsim.agent.AntFactory;
import it.unibo.antsim.controller.SimulationController;
import it.unibo.antsim.pheromone.Evaporation;
import it.unibo.antsim.pheromone.PheromoneMap;
import it.unibo.antsim.simulation.SimulationEngine;
import it.unibo.antsim.world.World;
import it.unibo.antsim.world.generation.GenerationParameters;
import it.unibo.antsim.world.generation.WorldGenerator;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Random;

public class Main extends Application {
    private static final int INITIAL_ANTS = 10000;

    @Override
    public void start(final Stage stage){
        final Random random = new Random();

        final GenerationParameters worldParameters = new GenerationParameters(
                80,
                100,
                20.0,
                20.0,
                0.85,
                0,
                1,
                10000,
                6
        );

        final WorldGenerator worldGenerator = new WorldGenerator(random);
        final World world = worldGenerator.generate(worldParameters);

        final PheromoneMap pheromoneMap = new PheromoneMap(
                worldParameters.rows(),
                worldParameters.cols(),
                worldParameters.cellWidth(),
                worldParameters.cellHeight(),
                100.0,
                new Evaporation(1.5)
        );

        final AcoParameters acoParameters = new AcoParameters(
                2.0,
                0.5,
                32.0,
                Math.PI/4.0,
                Math.PI/3.0,
                Math.PI/3.0
        );

        final SimulationEngine engine = new SimulationEngine(
                world,
                pheromoneMap,
                new AcoDecisionEngine(acoParameters, random),
                new AntFactory(35.0, random),
                worldGenerator,
                worldParameters
        );

        engine.setAgentCount(INITIAL_ANTS);

        final SimulationController controller = new SimulationController(engine, pheromoneMap, INITIAL_ANTS);

        final Scene scene = new Scene(controller.getView(), 1280, 800);
        stage.setTitle("AntSim");
        stage.setMinWidth(950);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();

        controller.render();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
