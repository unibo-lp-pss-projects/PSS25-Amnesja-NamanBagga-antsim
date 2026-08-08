package it.unibo.antsim.controller;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.antsim.pheromone.PheromoneField;
import it.unibo.antsim.simulation.SimulationEngine;
import it.unibo.antsim.simulation.SimulationStatus;
import it.unibo.antsim.view.SimulationView;
import it.unibo.antsim.view.WorldEdit;
import it.unibo.antsim.world.World;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.Objects;

/**
 * Simulation controller that manage user interaction and rendering.
 */
public final class SimulationController {
    private static final double BASE_TIME_STEP = 0.05;
    private static final int FRAME_MILLIS = 33;

    private final SimulationEngine engine;
    private final PheromoneField pheromoneField;
    private final SimulationView view;
    private final Timeline timeline;

    private double speedMultiplier = 1.0;
    private int requestedAntCount;

    /**
     * Instantiates a new Simulation controller.
     *
     * @param engine the engine
     * @param pheromoneField  the pheromone field
     * @param initialAntCount the initial ant count
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "The controller coordinates the simulation engine and pheromone field."
    )
    public SimulationController(final SimulationEngine engine, final PheromoneField pheromoneField, final int initialAntCount) {
        this.engine = Objects.requireNonNull(engine);
        this.pheromoneField = Objects.requireNonNull(pheromoneField);
        this.requestedAntCount = initialAntCount;
        this.view = new SimulationView(initialAntCount);
        this.timeline = new Timeline(new KeyFrame(Duration.millis(FRAME_MILLIS), event -> tick()));
        this.timeline.setCycleCount(Timeline.INDEFINITE);

        view.setOnStartPause(this::toggleSimulation);
        view.setOnReset(this::resetSimulation);
        view.setOnAntCountChanged(this::setAntCount);
        view.setOnSpeedChanged(value -> speedMultiplier = value);
        view.setOnCanvasResized(this::render);
        view.setOnGenerateWorld(this::generateScenario);
        view.setOnWorldEdit(this::applyWorldEdit);
    }

    /**
     * Get the view instance.
     *
     * @return the simulation view
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "JavaFX instance is needed to display the simulation."
    )
    public SimulationView getView() {
        return view;
    }

    /**
     * Renders the current state of the simulation.
     */
    public void render() {
        view.render(
                engine.getWorld(),
                pheromoneField,
                engine.getAnts(),
                engine.getStats()
        );

        view.setRunning(engine.getStatus() == SimulationStatus.RUNNING);
    }

    /**
     * Toggles the simulation status.
     */
    private void toggleSimulation() {
        if (engine.getStatus() == SimulationStatus.RUNNING) {
            engine.pause();
            timeline.stop();
        } else {
            if (engine.getStatus() == SimulationStatus.IDLE) {
                if (engine.getWorld().getNestIndex() == null) {
                    return;
                }
                engine.setAgentCount(requestedAntCount);
                engine.start();
        } else {
            engine.resume();
        }
            timeline.play();
        }
        render();
    }

    /**
     * Advances the simulation one time step and renders it.
     */
    private void tick() {
        if (engine.getStatus() != SimulationStatus.RUNNING) {
            return;
        }

        engine.step(BASE_TIME_STEP * speedMultiplier);
        render();
    }

    /**
     * Reset the current simulation.
     */
    private void resetSimulation() {
        timeline.stop();
        engine.reset();
        render();
    }

    /**
     * Sets the number of active agents in the simulation.
     *
     * @param count the new target count
     */
    private void setAntCount(final int count) {
        requestedAntCount = count;
        if (engine.getWorld().getNestIndex() != null) {
            engine.setAgentCount(count);
        }
        render();
    }

    /**
     * Generates a new world scenario.
     */
    private void generateScenario() {
        timeline.stop();
        engine.generateScenario();
        render();
    }

    /**
     * Applies interactive world edits from the user.
     *
     * @param edit the world edit operation
     */
    private void applyWorldEdit(final WorldEdit edit) {
        if (engine.getStatus() == SimulationStatus.RUNNING) {
            return;
        }
        final World world = engine.getWorld();

        switch (edit.tool()) {
            case NEST -> world.relocateNest(edit.cellIndex());
            case FOOD -> world.placeFood(edit.cellIndex(), edit.foodAmount());
            case OBSTACLE -> world.placeObstacle(edit.cellIndex());
            case ERASER -> world.clearCell(edit.cellIndex());
        }
        render();
    }
}
