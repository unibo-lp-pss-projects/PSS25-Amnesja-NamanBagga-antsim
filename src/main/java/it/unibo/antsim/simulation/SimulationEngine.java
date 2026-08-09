package it.unibo.antsim.simulation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.antsim.agent.AntGroup;
import it.unibo.antsim.agent.DecisionEngine;
import it.unibo.antsim.agent.AntFactory;
import it.unibo.antsim.agent.Ant;
import it.unibo.antsim.agent.AntState;
import it.unibo.antsim.pheromone.PheromoneField;
import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.World;
import it.unibo.antsim.world.generation.GenerationParameters;
import it.unibo.antsim.world.generation.WorldGenerator;
import java.util.Objects;
import java.util.Collections;
import java.util.List;

/**
 * This class is the skeleton implementation of the core simulation lifecycle.
 */
public class SimulationEngine {
    private static final double PHEROMONE_FOOD_DEPOSIT_RATE = 3.0;
    private static final double PHEROMONE_HOME_DEPOSIT_RATE = 3.0;
    private SimulationStatus status;
    private final SimulationClock clock;
    private int foodCollected;
    private World world;
    private final WorldGenerator worldGenerator;
    private final GenerationParameters generationParameters;
    private final PheromoneField pheromoneField;
    private final DecisionEngine decisionEngine;
    private final AntGroup antGroup;
    private final AntFactory antFactory;

    /**
     * Instantiates a new Simulation engine.
     *
     * @param world the world environment
     * @param pheromoneField the pheromone field
     * @param decisionEngine the decision engine for ant
     * @param antFactory the ant factory for generating ants
     * @param worldGenerator the world generator for scenario creation
     * @param generationParameters the generation parameters for world generation
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "the engine intentionally own the world and pheromone field of the simulation."
    )
    public SimulationEngine(
            final World world,
            final PheromoneField pheromoneField,
            final DecisionEngine decisionEngine,
            final AntFactory antFactory,
            final WorldGenerator worldGenerator,
            final GenerationParameters generationParameters
    ) {
        this.clock = new SimulationClock();
        this.status = SimulationStatus.IDLE;
        this.foodCollected = 0;
        this.world = Objects.requireNonNull(world);
        this.worldGenerator = Objects.requireNonNull(worldGenerator);
        this.generationParameters = Objects.requireNonNull(generationParameters);
        this.pheromoneField = Objects.requireNonNull(pheromoneField);
        this.decisionEngine = Objects.requireNonNull(decisionEngine);
        this.antGroup = new AntGroup();
        this.antFactory = Objects.requireNonNull(antFactory);
    }

    /**
     * Starts the simulation.
     */
    public void start() {
        if (status == SimulationStatus.RUNNING) {
            throw new IllegalStateException("Simulation already running!");
        }
        this.status = SimulationStatus.RUNNING;
    }

    /**
     * Pauses the simulation.
     */
    public void pause() {
        if (status != SimulationStatus.RUNNING) {
            throw new IllegalStateException("Cannot pause a not running simulation!");
        }
        this.status = SimulationStatus.PAUSED;
    }

    /**
     * Resumes the simulation.
     */
    public void resume() {
        if (status != SimulationStatus.PAUSED) {
            throw new IllegalStateException("Cannot resume a not paused simulation!");
        }
        this.status = SimulationStatus.RUNNING;
    }

    /**
     * Stops the simulation.
     */
    public void stop() {
        this.status = SimulationStatus.STOPPED;
    }

    /**
     * Resets the simulation to an empty scenario.
     */
    public void reset() {
        createEmptyScenario();
    }

    /**
     * Generates a new scenario with obstacles and food.
     */
    public void generateScenario() {
        status = SimulationStatus.IDLE;
        clock.reset();
        foodCollected = 0;

        antGroup.clear();
        pheromoneField.clear();
        world = worldGenerator.generate(generationParameters);
    }

    /**
     * Creates an empty scenario.
     */
    public void createEmptyScenario() {
        status = SimulationStatus.IDLE;
        clock.reset();
        foodCollected = 0;

        antGroup.clear();
        pheromoneField.clear();
        world = new World(
                generationParameters.rows(),
                generationParameters.cols(),
                generationParameters.cellWidth(),
                generationParameters.cellHeight()
        );
    }

    /**
     * Advances the simulation by one time step.
     *
     * @param dt delta time step duration
     */
    public void step(final double dt) {
        if (status != SimulationStatus.RUNNING) {
            throw new IllegalStateException("Simulation must be running!");
        }

        updateAgents(dt);
        foodPickup();
        nestDelivery();
        updateEnvironment(dt);
        clock.tick(dt);
    }

    /**
     * Updates position, direction and pheromone deposits for all ants.
     *
     * @param dt time step value
     */
    private void updateAgents(final double dt) {
        for (final Ant ant : antGroup.getAnts()) {
            final boolean returningToNest = ant.getState() == AntState.RETURNING_TO_NEST;

            ant.setAngle(decisionEngine.decideNextAngle(ant, world, pheromoneField));
            ant.move(dt, world);

            final CellIndex prev = ant.getPrevCell();
            if (prev == null) {
                continue;
            }

            if (returningToNest) {
                pheromoneField.deposit(prev, PheromoneField.PheromoneType.FOOD, PHEROMONE_FOOD_DEPOSIT_RATE * dt);
            } else {
                pheromoneField.deposit(prev, PheromoneField.PheromoneType.HOME, PHEROMONE_HOME_DEPOSIT_RATE * dt);
            }
        }
    }

    /**
     * Updates the environmental factor like pheromone decay.
     *
     * @param dt time step duration
     */
    private void updateEnvironment(final double dt) {
        pheromoneField.evaporate(dt);
    }

    /**
     * Gets the current world instance.
     *
     * @return the world instance
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "Controller and view need access to simulation world."
    )
    public World getWorld() {
        return world;
    }

    /**
     * Gets the current simulation status.
     *
     * @return the simulation status
     */
    public SimulationStatus getStatus() {
        return status;
    }

    /**
     * Get the simulation clock.
     *
     * @return the simulation clock
     */
    public SimulationClock getClock() {
        return clock;
    }

    /**
     * Gets a snapshot of the simulation statistics.
     *
     * @return the simulation statistics
     */
    public SimulationStatistics getStats() {
        return new SimulationStatistics(
                clock.getCurrentStep(),
                clock.getTotalTime(),
                antGroup.size(),
                foodCollected
        );
    }

    /**
     * Adds an ant to the simulation.
     *
     * @param ant the ant to add
     */
    public void addAnt(final Ant ant) {
        this.antGroup.addAnt(Objects.requireNonNull(ant));
    }

    /**
     * Gets an unmodifiable list of all active ants.
     *
     * @return the list of ants
     */
    public List<Ant> getAnts() {
        return Collections.unmodifiableList(antGroup.getAnts());
    }

    /**
     * Handles food pickup interaction for ants wandering near food.
     */
    public void foodPickup() {
        for (final Ant ant : antGroup.getAnts()) {
            if (ant.getState() != AntState.WANDERING) {
                continue;
            }

            world.findFoodCellNear(ant.getPosition())
                    .filter(world::consumeFood)
                    .ifPresent(ignored -> ant.pickFood());
        }
    }

    /**
     * Handles food delivery when ants returning to nest reach the nest cell.
     */
    public void nestDelivery() {
        for (final Ant ant : antGroup.getAnts()) {
            if (ant.getState() != AntState.RETURNING_TO_NEST || !world.isNestAt(ant.getPosition())) {
                continue;
            }
            foodCollected++;
            ant.dropFood();
        }
    }

    /**
     * This method dynamically adjusts the number of active ants in the simulation
     * and the new ants will be generated ad nest position.
     *
     * @param targetCount number of desired ants
     */
    public void setAgentCount(final int targetCount) {
        if (targetCount < 0) {
            throw new IllegalArgumentException("Agent count must be a positive value!");
        }

        final CellIndex nestIndex = Objects.requireNonNull(world.getNestIndex(), "There must be a nest before creating ants");
        final double cellWidth = world.getWidth() / world.getColumns();
        final double cellHeight = world.getHeight() / world.getRows();

        while (antGroup.size() < targetCount) {
            antGroup.addAnt(antFactory.generateAntInNest(nestIndex, cellWidth, cellHeight));
        }

        while (antGroup.size() > targetCount) {
            antGroup.removeLast();
        }
    }
}
