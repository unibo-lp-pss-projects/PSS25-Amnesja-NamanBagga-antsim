package it.unibo.antsim.view;

import it.unibo.antsim.agent.Ant;
import it.unibo.antsim.agent.AntRole;
import it.unibo.antsim.agent.AntState;
import it.unibo.antsim.pheromone.PheromoneField;
import it.unibo.antsim.simulation.SimulationStatistics;
import it.unibo.antsim.world.CellContent;
import it.unibo.antsim.world.CellIndex;
import it.unibo.antsim.world.World;
import it.unibo.antsim.world.WorldPosition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * JavaFX view component for rendering the simulation and UI components controls.
 */
public class SimulationView extends BorderPane {
    private static final double PHEROMONE_TRESHOLD = 0.001;
    private static final double FOOD_PHEROMONE_ALPHA_BASE = 0.05;
    private static final double FOOD_PHEROMONE_ALPHA_SCALE = 0.40;
    private static final double FOOD_PHEROMONE_MAX_ALPHA = 0.45;
    private static final double HOME_PHEROMONE_ALPHA_BASE = 0.04;
    private static final double HOME_PHEROMONE_ALPHA_SCALE = 0.31;
    private static final double HOME_PHEROMONE_MAX_ALPHA = 0.35;

    private static final double HALF_DIVISOR = 2.0;
    private static final double CENTER_OFFSET = 0.5;
    private static final double FOOD_SIZE = 0.50;
    private static final double FOOD_OFFSET = 0.15;
    private static final double NEST_SIZE = 0.70;
    private static final double NEST_OFFSET = 0.15;

    private static final double ANT_MIN_SIZE = 4.0;
    private static final double ANT_SIZE_SCALE = 7.0;
    private static final double ANT_SIZE_DIVISOR = 12.0;
    private static final double ANT_WING_OFFSET = 0.65;
    private static final double ANT_FOOD_CARRY_SIZE = 0.4;
    private static final double ANT_FOOD_CARRY_SCALE = 0.8;
    private static final int ANT_BODY_POLYGONS = 3;

    private static final int DEFAULT_FOOD_Q = 1_000;
    private static final int MIN_ANT = 1;
    private static final int MAX_ANT = 25_000;
    private static final int MIN_FOOD_Q = 1;
    private static final int MAX_FOOD_Q = 100_000;
    private static final double MIN_SPEED = 0.25;
    private static final double MAX_SPEED = 5.0;
    private static final double DEFAULT_SPEED = 1.0;
    private static final double SPEED_TICK_UNIT = 1.0;

    private static final int TOOLBAR_SPACE = 10;
    private static final int TOOLBAR_PADDING = 10;
    private static final int HBOX_BUTTON_SPACE = 8;
    private static final int SIDE_PANEL_SPACE = 10;
    private static final int SIDE_PANEL_PADDING = 16;
    private static final int SIDE_PANEL_WIDTH = 260;
    private static final int CANVAS_PADDING = 12;
    private static final int CANVAS_BIND = 24;

    private static final int TITLE_SIZE = 15;

    private static final int CANVASBG_R = 25;
    private static final int CANVASBG_B = 35;
    private static final int CANVASBG_G = 29;

    private static final int FOOD_PHEROMONE_R = 30;
    private static final int FOOD_PHEROMONE_B = 220;
    private static final int FOOD_PHEROMONE_G = 120;
    private static final int HOME_PHEROMONE_R = 90;
    private static final int HOME_PHEROMONE_B = 130;
    private static final int HOME_PHEROMONE_G = 255;

    private static final int GRID_R = 60;
    private static final int GRID_B = 67;
    private static final int GRID_G = 78;
    private static final double GRID_OPACITY = 0.35;

    private final Canvas canvas;
    private final Label statusLabel = new Label("Stato: IDLE");
    private final Label stepLabel = new Label("Step: 0");
    private final Label timeLabel = new Label("Tempo: 0,0 s");
    private final Label antsLabel = new Label("Formiche: 0");
    private final Label foodLabel = new Label("Cibo raccolto: 0");
    private final Button startPauseButton = new Button("Avvia");

    private Runnable onStartPause = () -> { };
    private Runnable onReset = () -> { };
    private Consumer<Integer> onAntCountChanged = ignored -> { };
    private Consumer<Double> onSpeedChanged = ignored -> { };
    private Consumer<WorldEdit> onWorldEdit = ignored -> { };
    private Runnable onCanvasResized = () -> { };
    private Runnable onGenerateWorld = () -> { };

    private EditorTool selectedTool = EditorTool.NEST;
    private int selectedFoodQuantity = DEFAULT_FOOD_Q;

    private World renderWorld;
    private double renderScale;
    private double renderXOffset;
    private double renderYOffset;

    /**
     * Instantiates a new Simulation view.
     *
     * @param initialAntCount the initial ant count
     */
    public SimulationView(final int initialAntCount) {
        canvas = new Canvas();

        canvas.setOnMousePressed(event -> editCellAt(event.getX(), event.getY()));
        canvas.setOnMouseDragged(event -> {
            if (selectedTool != EditorTool.NEST) {
                editCellAt(event.getX(), event.getY());
            }
        });

        setTop(createToolbar());
        setCenter(createCanvasArea());
        setRight(createSidePanel(initialAntCount));

        setStyle("-fx-background-color: #171a1f;");
    }

    /**
     * Sets the start/pause.
     *
     * @param callback the action
     */
    public void setOnStartPause(final Runnable callback) {
         onStartPause = Objects.requireNonNull(callback);
    }

    /**
     * sets the reset.
     *
     * @param callback the action
     */
    public void setOnReset(final Runnable callback) {
        onReset = Objects.requireNonNull(callback);
    }

    /**
     * Sets the ant count.
     *
     * @param callback the action
     */
    public void setOnAntCountChanged(final Consumer<Integer> callback) {
        onAntCountChanged = Objects.requireNonNull(callback);
    }

    /**
     * Sets the speed changed.
     *
     * @param callback the action
     */
    public void setOnSpeedChanged(final Consumer<Double> callback) {
        onSpeedChanged = Objects.requireNonNull(callback);
    }

    /**
     * Sets the canvas resized.
     *
     * @param callback the action
     */
    public void setOnCanvasResized(final Runnable callback) {
        onCanvasResized = Objects.requireNonNull(callback);
    }

    /**
     * Sets the scenario generation.
     *
     * @param callback the action
     */
    public void setOnGenerateWorld(final Runnable callback) {
        onGenerateWorld = Objects.requireNonNull(callback);
    }

    /**
     * Sets the world edit.
     *
     * @param callback the action
     */
    public void setOnWorldEdit(final Consumer<WorldEdit> callback) {
        onWorldEdit = Objects.requireNonNull(callback);
    }

    /**
     * Updates UI buttons or labels based on how the simulation is running.
     *
     * @param running true if the simulation is running
     */
    public void setRunning(final boolean running) {
        startPauseButton.setText(running ? "Pausa" : "Avvia");
        statusLabel.setText(running ? "Stato: ESECUZIONE" : "Stato: PAUSA");
    }

    /**
     * Renders the world, pheromones, ants and statistics.
     *
     * @param world the world environment
     * @param pheromoneField the pheromone field
     * @param ants the active ants list
     * @param statistics simulation statistics
     */
    public void render(final World world,
                       final PheromoneField pheromoneField,
                       final List<Ant> ants,
                       final SimulationStatistics statistics
    ) {
        updateStatistics(statistics);

        if (canvas.getWidth() <= 0 || canvas.getHeight() <= 0) {
            return;
        }

        final GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.rgb(CANVASBG_R, CANVASBG_G, CANVASBG_B));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        final double scale = Math.min(
                canvas.getWidth() / world.getWidth(),
                canvas.getHeight() / world.getHeight()
        );

        final double worldScreenWidth = world.getWidth() * scale;
        final double worldScreenHeight = world.getHeight() * scale;
        final double xOffset = (canvas.getWidth() - worldScreenWidth) / HALF_DIVISOR;
        final double yOffset = (canvas.getHeight() - worldScreenHeight) / HALF_DIVISOR;

        renderWorld = world;
        renderScale = scale;
        renderXOffset = xOffset;
        renderYOffset = yOffset;

        drawPheromones(gc, world, pheromoneField, scale, xOffset, yOffset);
        drawWorld(gc, world, scale, xOffset, yOffset);
        drawAnts(gc, ants, scale, xOffset, yOffset);
    }

    /**
     * Creates the top toolbar HBox containing control buttons.
     *
     * @return the toolbar
     */
    private HBox createToolbar() {
        final Button generateButton = new Button("Genera scenario");
        final Button resetButton = new Button("Mappa vuota");

        startPauseButton.setOnAction(event -> onStartPause.run());
        generateButton.setOnAction(event -> onGenerateWorld.run());
        resetButton.setOnAction(event -> onReset.run());

        final HBox toolbar = new HBox(TOOLBAR_SPACE, startPauseButton, generateButton, resetButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(TOOLBAR_PADDING));
        toolbar.setStyle("-fx-background-color: #252a33;");

        return toolbar;
    }

    /**
     * Creates the center StackPane wrapping the canvas.
     *
     * @return canvas container pane
     */
    private StackPane createCanvasArea() {
        final StackPane pane = new StackPane(canvas);
        pane.setPadding(new Insets(CANVAS_PADDING));
        pane.setStyle("-fx-background-color: #111318;");

        canvas.widthProperty().bind(pane.widthProperty().subtract(CANVAS_BIND));
        canvas.heightProperty().bind(pane.heightProperty().subtract(CANVAS_BIND));

        canvas.widthProperty().addListener(
                (observable, oldValue, newValue) -> onCanvasResized.run());
        canvas.heightProperty().addListener(
                (observable, oldValue, newValue) -> onCanvasResized.run());

        return pane;
    }

    /**
     * Creates the right sidebar containing controls and statistics.
     *
     * @param initialAntCount initial ant count
     * @return the side panel VBox
     */
    private VBox createSidePanel(final int initialAntCount) {
        final Label statsTitle = sectionTitle("STATISTICHE");
        final Label parameterTitle = sectionTitle("PARAMETRI");

        final Spinner<Integer> antSpinner = createIntegerSpinner(MIN_ANT, MAX_ANT, initialAntCount);
        antSpinner.valueProperty().addListener(
                (observable, oldValue, newValue) -> onAntCountChanged.accept(newValue));

        final Slider speedSlider = new Slider(MIN_SPEED, MAX_SPEED, DEFAULT_SPEED);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(SPEED_TICK_UNIT);
        speedSlider.valueProperty().addListener(
                (observable, oldValue, newValue) ->
                        onSpeedChanged.accept(newValue.doubleValue())
        );

        final Label editorTitle = sectionTitle("EDITOR MAPPA");
        final ToggleGroup tools = new ToggleGroup();

        final ToggleButton nestTool = createToolButton("NIDO", EditorTool.NEST, tools);
        final ToggleButton foodTool = createToolButton("CIBO", EditorTool.FOOD, tools);
        final ToggleButton obstacleTool = createToolButton("OSTACOLO", EditorTool.OBSTACLE, tools);
        final ToggleButton eraserTool = createToolButton("CANCELLA", EditorTool.ERASER, tools);

        nestTool.setSelected(true);

        final Spinner<Integer> foodAmountSpinner = createIntegerSpinner(MIN_FOOD_Q, MAX_FOOD_Q, DEFAULT_FOOD_Q);
        foodAmountSpinner.valueProperty().addListener(
                (observable, oldValue, newValue) -> selectedFoodQuantity = newValue.intValue());

        final Label foodAmountLabel = new Label("QUANTITA' CIBO: 1000");
        foodAmountSpinner.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
            selectedFoodQuantity = newValue.intValue();
            foodAmountLabel.setText("QUANTITA' CIBO: " + selectedFoodQuantity);
        });

        final VBox panel = new VBox(
                SIDE_PANEL_SPACE,
                statsTitle,
                statusLabel,
                stepLabel,
                timeLabel,
                antsLabel,
                foodLabel,
                new Separator(),
                editorTitle,
                new HBox(HBOX_BUTTON_SPACE, nestTool, foodTool),
                new HBox(HBOX_BUTTON_SPACE, obstacleTool, eraserTool),
                foodAmountLabel,
                foodAmountSpinner,
                new Separator(),
                parameterTitle,
                new Label("Numero Formiche"),
                antSpinner,
                new Label("Velocità"),
                speedSlider
        );

        panel.setPadding(new Insets(SIDE_PANEL_PADDING));
        panel.setPrefWidth(SIDE_PANEL_WIDTH);
        panel.setStyle(
                "-fx-background-color: #252a33;" + "-fx-test-fill: white;"
        );

        for (final Node node : panel.getChildren()) {
            if (node instanceof Label label) {
                label.setTextFill(Color.web("#e8edf2"));
            }
        }
        return panel;
    }

    /**
     * Creates the title section Label.
     *
     * @param text title text
     * @return Label
     */
    private Label sectionTitle(final String text) {
        final Label title = new Label(text);
        title.setFont(Font.font(TITLE_SIZE));
        title.setTextFill(Color.web("#70d6ff"));
        return title;
    }

    /**
     * Updates the statistics label text.
     *
     * @param stats statistics snapshot
     */
    private void updateStatistics(final SimulationStatistics stats) {
        stepLabel.setText("Step: " + stats.currentStep());
        timeLabel.setText(String.format("Tempo: %.1f s", stats.timeElapsed()));
        antsLabel.setText("Formiche: " + stats.activeAnts());
        foodLabel.setText("Cibo raccolto: " + stats.foodCollected());
    }

    /**
     * Draws pheromone on canvas.
     *
     * @param gc the graphic context
     * @param world world environment
     * @param pheromoneField pheromone field
     * @param scale scaling factor
     * @param xOffset x offset
     * @param yOffset y offset
     */
    private void drawPheromones(final GraphicsContext gc,
                                final World world,
                                final PheromoneField pheromoneField,
                                final double scale,
                                final double xOffset,
                                final double yOffset
    ) {
        final double cellWidth = world.getWidth() / world.getColumns();
        final double cellHeight = world.getHeight() / world.getRows();

        for (int row = 0; row < world.getRows(); row++) {
            for (int col = 0; col < world.getColumns(); col++) {
                final WorldPosition centerCell = new WorldPosition(
                        (col + CENTER_OFFSET) * cellWidth,
                        (row + CENTER_OFFSET) * cellHeight
                );

                final double food = pheromoneField.level(centerCell, PheromoneField.PheromoneType.FOOD);
                final double home = pheromoneField.level(centerCell, PheromoneField.PheromoneType.HOME);

                final double x = xOffset + col * cellWidth * scale;
                final double y = yOffset + row * cellHeight * scale;
                final double width = cellWidth * scale;
                final double height = cellHeight * scale;

                final double foodAlpha = food <= PHEROMONE_TRESHOLD ? 0.0
                        : Math.min(FOOD_PHEROMONE_MAX_ALPHA,
                        FOOD_PHEROMONE_ALPHA_BASE + FOOD_PHEROMONE_ALPHA_SCALE * Math.sqrt(food));
                final double homeAlpha = home <= PHEROMONE_TRESHOLD ? 0.0
                        : Math.min(HOME_PHEROMONE_MAX_ALPHA,
                                    HOME_PHEROMONE_ALPHA_BASE + HOME_PHEROMONE_ALPHA_SCALE * Math.sqrt(home));

                if (food > PHEROMONE_TRESHOLD) {
                    gc.setFill(Color.rgb(FOOD_PHEROMONE_R, FOOD_PHEROMONE_G, FOOD_PHEROMONE_B, foodAlpha));
                    gc.fillRect(x, y, width, height);
                }

                if (home > PHEROMONE_TRESHOLD) {
                    gc.setFill(Color.rgb(HOME_PHEROMONE_R, HOME_PHEROMONE_G, HOME_PHEROMONE_B, homeAlpha));
                    gc.fillRect(x, y, width, height);
                }
            }
        }
    }

    /**
     * Draws the grid, obstacles, food and the nest on canvas.
     *
     * @param gc graphic context
     * @param world world environment
     * @param scale scaling factor
     * @param xOffset x offset
     * @param yOffset y offset
     */
    private void drawWorld(final GraphicsContext gc,
                           final World world,
                           final double scale,
                           final double xOffset,
                           final double yOffset
    ) {
        final double cellWidth = world.getWidth() / world.getColumns();
        final double cellHeight = world.getHeight() / world.getRows();

        for (int row = 0; row < world.getRows(); row++) {
            for (int col = 0; col < world.getColumns(); col++) {
                final CellIndex index = new CellIndex(row, col);
                final CellContent content = world.getGrid().getCellAt(index).getCellContent();

                final double x = xOffset + col * cellWidth * scale;
                final double y = yOffset + row * cellHeight * scale;
                final double width = cellWidth * scale;
                final double height = cellHeight * scale;

                gc.setStroke(Color.rgb(GRID_R, GRID_G, GRID_B, GRID_OPACITY));
                gc.strokeRect(x, y, width, height);

                if (content instanceof CellContent.Obstacle) {
                    gc.setFill(Color.web("#59616d"));
                    gc.fillRect(x, y, width, height);
                } else if (content instanceof CellContent.Food food) {
                    final double size = Math.min(width, height) * 0.58;
                    gc.setFill(Color.web("#72d572"));
                    gc.fillOval(
                            x + (width - size) / HALF_DIVISOR,
                            y + (height - size) / HALF_DIVISOR,
                            size,
                            size
                    );

                    gc.setFill(Color.web("#102610"));
                    gc.fillText(
                            String.valueOf(food.quantity()),
                            x + width * FOOD_OFFSET,
                            y + height * FOOD_OFFSET
                    );
                } else if (content instanceof CellContent.Nest) {
                    gc.setFill(Color.web("#ad7650"));
                    gc.fillOval(
                            x + width * NEST_OFFSET,
                            y + height * NEST_OFFSET,
                            width * NEST_SIZE,
                            height * NEST_SIZE
                    );
                }
            }
        }
    }

    /**
     * Draws active ants on the canvas.
     *
     * @param gc graphic context
     * @param ants list of active ants
     * @param scale scaling factor
     * @param xOffset x offset
     * @param yOffset y offset
     */
    final void drawAnts(final GraphicsContext gc,
                        final List<Ant> ants,
                        final double scale,
                        final double xOffset,
                        final double yOffset
    ) {
        for (final Ant ant : ants) {
            final double x = xOffset + ant.getPosition().x() * scale;
            final double y = yOffset + ant.getPosition().y() * scale;
            final double size = Math.max(ANT_MIN_SIZE, ANT_SIZE_SCALE * scale / ANT_SIZE_DIVISOR);

            gc.save();
            gc.translate(x, y);
            gc.rotate(Math.toDegrees(ant.getAngle()));

            gc.setFill(colorFor(ant));
            gc.fillPolygon(
                    new double[] {size, -size, -size},
                    new double[] {0, -size * ANT_WING_OFFSET, size * ANT_WING_OFFSET},
                    ANT_BODY_POLYGONS
            );

            if (ant.isCarryingFood()) {
                gc.setFill(Color.web("#72d572"));
                gc.fillOval(-size * ANT_FOOD_CARRY_SIZE,
                        -size * ANT_FOOD_CARRY_SIZE,
                        size * ANT_FOOD_CARRY_SCALE,
                        size * ANT_FOOD_CARRY_SCALE
                );
            }
            gc.restore();
        }
    }

    /**
     * Display color for ants based on state and role.
     *
     * @param ant target ant
     * @return color for rendering
     */
    private Color colorFor(final Ant ant) {
        if (ant.getState() == AntState.RETURNING_TO_NEST) {
            return Color.web("#ff9f43");
        }
        if (ant.getRole() == AntRole.EXPLORER) {
            return Color.web("#d980fa");
        }
        return Color.web("#f5f6fa");
    }

    /**
     * Converts mouse coordinates to cell index and notify edit action.
     *
     * @param screenX screen mouse x coordinate
     * @param screenY screen mouse y coordinate.
     */
    private void editCellAt(final double screenX, final double screenY) {
        if (renderWorld == null || renderScale <= 0) {
            return;
        }

        final double worldX = (screenX - renderXOffset) / renderScale;
        final double worldY = (screenY - renderYOffset) / renderScale;

        if (worldX < 0 || worldX >= renderWorld.getWidth() || worldY < 0 || worldY >= renderWorld.getHeight()) {
            return;
        }

        final CellIndex cell = renderWorld.convertToCellIndex(new WorldPosition(worldX, worldY));

        onWorldEdit.accept(new WorldEdit(cell, selectedTool, selectedFoodQuantity));
    }

    /**
     * Helper for creating toogle buttons fdor the toolbar.
     *
     * @param text button text
     * @param tool editor tool
     * @param group toggle group
     * @return the configured ToggleButton
     */
    private ToggleButton createToolButton(final String text, final EditorTool tool, final ToggleGroup group) {
        final ToggleButton button = new ToggleButton(text);
        button.setToggleGroup(group);
        button.setOnAction(event -> selectedTool = tool);
        return button;
    }

    /**
     * Helper for creating an integer Spinner.
     *
     * @param min minimum value
     * @param max maximum value
     * @param initialValue intiial value
     * @return the configured Spinner
     */
    private Spinner<Integer> createIntegerSpinner(final int min, final int max, final int initialValue) {
        final Spinner<Integer> spinner = new Spinner<>(min, max, initialValue);

        spinner.setEditable(true);

        spinner.getEditor().setOnAction(event -> commitIntegerSpinner(spinner, min, max));
        spinner.getEditor().
                focusedProperty().
                addListener((observable, oldValue, newValue) -> {
                    if (!isFocused()) {
                        commitIntegerSpinner(spinner, min, max);
                    }
                });

        return spinner;
    }

    /**
     * Commits text input in spinner (prevents values very small or very big).
     *
     * @param spinner target spinner
     * @param min minimum bound
     * @param max max bound
     */
    private void commitIntegerSpinner(final Spinner<Integer> spinner, final int min, final int max) {
        try {
            final int typedvalue = Integer.parseInt(spinner.getEditor().getText());
            spinner.getValueFactory().setValue(Math.clamp(typedvalue, min, max));
        } catch (final NumberFormatException ignored) {
            spinner.getEditor().setText(String.valueOf(spinner.getValue()));
        }
    }
}
