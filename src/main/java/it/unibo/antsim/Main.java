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

/**
 * The type Main.
 */
public final class Main extends Application {
    private static final int INITIAL_ANTS = 100;
    private static final int WORLD_ROWS = 80;
    private static final int WORLD_COLUMNS = 100;
    private static final int CELL_WIDTH = 20;
    private static final int CELL_HEIGHT = 20;
    private static final double ROCK_PROBABILITY = 0.85;
    private static final int ROCK_CLUSTERS = 0;
    private static final int FOOD_CLUSTERS = 10;
    private static final int FOOD_PER_CELL = 1_000;
    private static final int NEST_CLEARANCE_RADIUS = 6;
    private static final double MAX_PHEROMONE_LEVEL = 100.0;
    private static final double EVAPORATION_RATE = 1.0;
    private static final double ACO_ALPHA = 1.0;
    private static final double ACO_BETA = 0.5;
    private static final double SENSOR_RANGE = 32.0;
    private static final double SENSOR_ANGLE = Math.PI / 4.0;
    private static final double RANDOM_FACTOR = Math.PI / 3.0;
    private static final double TURN_STRENGTH = Math.PI / 3.0;
    private static final double ANT_SPEED = 35.0;
    private static final int WINDOW_WIDTH = 1_280;
    private static final int WINDOW_HEIGHT = 800;
    private static final int MIN_WINDOW_WIDTH = 950;
    private static final int MIN_WINDOW_HEIGHT = 600;
    private static final String APPLICATION_TITLE = "AntSim";

    @Override
    public void start(final Stage stage) {
        final Random random = new Random();

        final GenerationParameters worldParameters = new GenerationParameters(
                WORLD_ROWS,
                WORLD_COLUMNS,
                CELL_WIDTH,
                CELL_HEIGHT,
                ROCK_PROBABILITY,
                ROCK_CLUSTERS,
                FOOD_CLUSTERS,
                FOOD_PER_CELL,
                NEST_CLEARANCE_RADIUS
        );

        final WorldGenerator worldGenerator = new WorldGenerator(random);
        final World world = new World(
                worldParameters.rows(),
                worldParameters.cols(),
                worldParameters.cellWidth(),
                worldParameters.cellHeight()
        );

        final PheromoneMap pheromoneMap = new PheromoneMap(
                worldParameters.rows(),
                worldParameters.cols(),
                worldParameters.cellWidth(),
                worldParameters.cellHeight(),
                MAX_PHEROMONE_LEVEL,
                new Evaporation(EVAPORATION_RATE)
        );

        final AcoParameters acoParameters = new AcoParameters(
                ACO_ALPHA,
                ACO_BETA,
                SENSOR_RANGE,
                SENSOR_ANGLE,
                RANDOM_FACTOR,
                TURN_STRENGTH
        );

        final SimulationEngine engine = new SimulationEngine(
                world,
                pheromoneMap,
                new AcoDecisionEngine(acoParameters, random),
                new AntFactory(ANT_SPEED, random),
                worldGenerator,
                worldParameters
        );

        final SimulationController controller = new SimulationController(engine, pheromoneMap, INITIAL_ANTS);

        final Scene scene = new Scene(controller.getView(), WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setTitle(APPLICATION_TITLE);
        stage.setMinWidth(MIN_WINDOW_WIDTH);
        stage.setMinHeight(MIN_WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.show();

        controller.render();
    }

    /**
     * This is important for launching the app.
     *
     * @param args final main arguments
     */
    static void main(final String[] args) {
        launch(args);
    }
}
