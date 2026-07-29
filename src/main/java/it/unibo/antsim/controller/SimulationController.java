package it.unibo.antsim.controller;

import it.unibo.antsim.pheromone.PheromoneField;
import it.unibo.antsim.simulation.SimulationEngine;
import it.unibo.antsim.simulation.SimulationStatus;
import it.unibo.antsim.view.SimulationView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.Objects;

public class SimulationController {
    private static final double BASE_TIME_STEP = 0.05;
    private static final int FRAME_MILLIS = 33;

    private final SimulationEngine engine;
    private final PheromoneField pheromoneField;
    private final SimulationView view;
    private final Timeline timeline;

    private double speedMultiplier = 1.0;
    private int requestedAntCount;

    public SimulationController(final SimulationEngine engine, final PheromoneField pheromoneField, final int initialAntCount){
        this.engine = Objects.requireNonNull(engine);
        this.pheromoneField = Objects.requireNonNull(pheromoneField);
        this.requestedAntCount = initialAntCount;
        this.view = new SimulationView();
        this.timeline = new Timeline(new KeyFrame(Duration.millis(FRAME_MILLIS), event -> tick()));
        this.timeline.setCycleCount(Timeline.INDEFINITE);

        view.setOnStartPause(this::toggleSimulation);
        view.setOnReset(this::resetSimulation);
        view.setOnAntCountChanged(this::setAntCount);
        view.setOnSpeedChanged(value -> speedMultiplier = value);
        view.setOnCanvasResized(this::render);
    }

    public SimulationView getView(){
        return view;
    }

    public void render(){
        view.render(
                engine.getWorld(),
                pheromoneField,
                engine.getAnts(),
                engine.getStats()
        );

        view.setRunning(engine.getStatus() == SimulationStatus.RUNNING);
    }

    private void toggleSimulation(){
        if (engine.getStatus() == SimulationStatus.RUNNING) {
            engine.pause();
            timeline.stop();
        }else{
            if(engine.getStatus() == SimulationStatus.IDLE || engine.getStatus() == SimulationStatus.STOPPED){
                engine.start();
            }else{
                engine.resume();
            }
            timeline.play();
        }
        render();
    }

    private void tick(){
        if(engine.getStatus() != SimulationStatus.RUNNING){
            return;
        }

        engine.step(BASE_TIME_STEP * speedMultiplier);
        render();
    }

    private void resetSimulation(){
        timeline.stop();
        engine.reset();
        engine.setAgentCount(requestedAntCount);
        render();
    }

    private void setAntCount(final int count){
        requestedAntCount = count;
        engine.setAgentCount(count);
        render();
    }
}
