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
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import javax.swing.text.LabelView;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class SimulationView extends BorderPane {
    private static final double PHEROMONE_MAX_LEVEL = 100.0;

    private final Canvas canvas;
    private final Label statusLabel = new Label("Stato: IDLE");
    private final Label stepLabel = new Label("Step: 0");
    private final Label timeLabel = new Label("Tempo: 0,0 s");
    private final Label antsLabel = new Label("Formiche: 0");
    private final Label foodLabel = new Label("Cibo raccolto: 0");
    private final Button startPauseButton = new Button("Avvia");

    private Runnable onStartPause = () -> {};
    private Runnable onReset = () -> {};
    private Consumer<Integer> onAntCountChanged = ignored -> {};
    private Consumer<Double> onSpeedChanged = ignored -> {};
    private Consumer<WorldEdit> onWorldEdit = ignored -> {};
    private Runnable onCanvasResized = () -> {};
    private Runnable onGenerateWorld = () -> {};

    private EditorTool selectedTool = EditorTool.NEST;
    private int selectedFoodQuantity = 1000;

    private World renderWorld;
    private double renderScale;
    private double renderXOffset;
    private double renderYOffset;

    public SimulationView(final int initialAntCount){
        canvas = new Canvas();

        canvas.setOnMousePressed(event -> editCellAt(event.getX(), event.getY()));
        canvas.setOnMouseDragged(event ->{
            if(selectedTool != EditorTool.NEST){
                editCellAt(event.getX(), event.getY());
            }
        });

        setTop(createToolbar());
        setCenter(createCanvasArea());
        setRight(createSidePanel(initialAntCount));

        setStyle("-fx-background-color: #171a1f;");
    }

    public void setOnStartPause(final Runnable callback){
         onStartPause = Objects.requireNonNull(callback);
    }

    public void setOnReset(final Runnable callback){
        onReset = Objects.requireNonNull(callback);
    }

    public void setOnAntCountChanged(final Consumer<Integer> callback){
        onAntCountChanged = Objects.requireNonNull(callback);
    }

    public void setOnSpeedChanged(final Consumer<Double> callback){
        onSpeedChanged = Objects.requireNonNull(callback);
    }

    public void setOnCanvasResized(final Runnable callback){
        onCanvasResized = Objects.requireNonNull(callback);
    }

    public void setOnGenerateWorld(final Runnable callback){
        onGenerateWorld = Objects.requireNonNull(callback);
    }
    public void setOnWorldEdit(final Consumer<WorldEdit> callback) {
        onWorldEdit = Objects.requireNonNull(callback);
    }
    public void setRunning(final boolean running){
        startPauseButton.setText(running ? "Pausa" : "Avvia");
        statusLabel.setText(running ? "Stato: ESECUZIONE" : "Stato: PAUSA");
    }

    public void render(final World world, final PheromoneField pheromoneField, final List<Ant> ants, final SimulationStatistics statistics){
        updateStatistics(statistics);

        if(canvas.getWidth()<=0 || canvas.getHeight()<=0){
            return;
        }

        final GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.rgb(25, 29, 35));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        final double scale = Math.min(
                canvas.getWidth() / world.getWidth(),
                canvas.getHeight() / world.getHeight()
        );

        final double worldScreenWidth = world.getWidth() * scale;
        final double worldScreenHeight = world.getHeight() * scale;
        final double xOffset = (canvas.getWidth() - worldScreenWidth) / 2.0;
        final double yOffset = (canvas.getHeight() - worldScreenHeight) / 2.0;

        renderWorld = world;
        renderScale = scale;
        renderXOffset = xOffset;
        renderYOffset = yOffset;

        drawPheromones(gc, world, pheromoneField, scale, xOffset, yOffset);
        drawWorld(gc, world, scale, xOffset, yOffset);
        drawAnts(gc, ants, scale, xOffset, yOffset);
    }

    private HBox createToolbar(){
        final Button generateButton = new Button("Genera scenario");
        final Button resetButton = new Button("Mappa vuota");

        startPauseButton.setOnAction(event -> onStartPause.run());
        generateButton.setOnAction(event -> onGenerateWorld.run());
        resetButton.setOnAction(event -> onReset.run());

        final HBox toolbar = new HBox(10, startPauseButton, generateButton, resetButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10));
        toolbar.setStyle("-fx-background-color: #252a33;");

        return toolbar;
    }

    private StackPane createCanvasArea(){
        final StackPane pane = new StackPane(canvas);
        pane.setPadding(new Insets(12));
        pane.setStyle("-fx-background-color: #111318;");

        canvas.widthProperty().bind(pane.widthProperty().subtract(24));
        canvas.heightProperty().bind(pane.heightProperty().subtract(24));

        canvas.widthProperty().addListener((observable, oldValue, newValue) -> onCanvasResized.run());
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> onCanvasResized.run());

        return pane;
    }

    private VBox createSidePanel(final int initialAntCount){
        final Label statsTitle = sectionTitle("STATISTICHE");
        final Label parameterTitle = sectionTitle("PARAMETRI");

        final Spinner<Integer> antSpinner = createIntegerSpinner(1, 25_000, initialAntCount);
        antSpinner.valueProperty().addListener((observable, oldValue, newValue) -> onAntCountChanged.accept(newValue));

        final Slider speedSlider = new Slider(0.25, 5.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(1.0);
        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> onSpeedChanged.accept(newValue.doubleValue()));

        final Label editoTitle = sectionTitle("EDITOR MAPPA");
        final ToggleGroup tools = new ToggleGroup();

        final ToggleButton nestTool = createToolButton("NIDO", EditorTool.NEST, tools);
        final ToggleButton foodTool = createToolButton("CIBO", EditorTool.FOOD, tools);
        final ToggleButton obstacleTool = createToolButton("OSTACOLO", EditorTool.OBSTACLE, tools);
        final ToggleButton eraserTool = createToolButton("CANCELLA", EditorTool.ERASER, tools);

        nestTool.setSelected(true);

        final Spinner<Integer> foodAmountSpinner = createIntegerSpinner(1, 100_000, 1_000);
        foodAmountSpinner.valueProperty().addListener((observable, oldValue, newValue) -> selectedFoodQuantity = newValue.intValue());

        final Label foodAmountLabel = new Label("QUANTITA' CIBO: 1000");
        foodAmountSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            selectedFoodQuantity = newValue.intValue();
            foodAmountLabel.setText("QUANTITA' CIBO: " + selectedFoodQuantity);
        });

        final VBox panel = new VBox(
                10,
                statsTitle,
                statusLabel,
                stepLabel,
                timeLabel,
                antsLabel,
                foodLabel,
                new Separator(),
                editoTitle,
                new HBox(8, nestTool, foodTool),
                new HBox(8, obstacleTool, eraserTool),
                foodAmountLabel,
                foodAmountSpinner,
                new Separator(),
                parameterTitle,
                new Label("Numero Formiche"),
                antSpinner,
                new Label("Velocità"),
                speedSlider
        );

        panel.setPadding(new Insets(16));
        panel.setPrefWidth(260);
        panel.setStyle(
                "-fx-background-color: #252a33;" + "-fx-test-fill: white;"
        );

        for(Node node : panel.getChildren()){
            if(node instanceof Label label){
                label.setTextFill(Color.web("#e8edf2"));
            }
        }
        return panel;
    }

    private Label sectionTitle(final String text){
        final Label title = new Label(text);
        title.setFont(Font.font(15));
        title.setTextFill(Color.web("#70d6ff"));
        return title;
    }

    private void updateStatistics(final SimulationStatistics stats){
        stepLabel.setText("Step: " + stats.currentStep());
        timeLabel.setText(String.format("Tempo: %.1f s", stats.timeElapsed()));
        antsLabel.setText("Formiche: " + stats.activeAnts());
        foodLabel.setText("Cibo raccolto: " + stats.foodCollected());
    }

    private void drawPheromones(final GraphicsContext gc, final World world, final PheromoneField pheromoneField, final double scale, final double xOffset, final double yOffset){
        final double cellWidth = world.getWidth() / world.getColumns();
        final double cellHeight = world.getHeight() / world.getRows();

        for(int row=0; row<world.getRows(); row++){
            for(int col=0; col<world.getColumns(); col++){
                final WorldPosition centerCell = new WorldPosition(
                        (col + 0.5) * cellWidth,
                        (row + 0.5) * cellHeight
                );

                final double food = pheromoneField.level(centerCell, PheromoneField.PheromoneType.FOOD);
                final double home = pheromoneField.level(centerCell, PheromoneField.PheromoneType.HOME);

                final double x = xOffset + col * cellWidth * scale;
                final double y = yOffset + row * cellHeight * scale;
                final double width = cellWidth * scale;
                final double height = cellHeight * scale;

                final double foodAlpha = food <= 0.001 ? 0.0 : Math.min(0.45, 0.05 + 0.40 * Math.sqrt(food));
                final double homeAlpha = home <= 0.001 ? 0.0 : Math.min(0.35, 0.04 + 0.31 * Math.sqrt(home));

                if(food > 0.001){
                    gc.setFill(Color.rgb(30, 220, 120, foodAlpha));
                    gc.fillRect(x, y, width, height);
                }

                if(home > 0.001){
                    gc.setFill(Color.rgb(90, 130, 255, homeAlpha));
                    gc.fillRect(x, y, width, height);
                }
            }
        }
    }

    private void drawWorld(final GraphicsContext gc, final World world, final double scale, final double xOffset, final double yOffset){
        final double cellWidth = world.getWidth() / world.getColumns();
        final double cellHeight = world.getHeight() / world.getRows();

        for(int row=0; row<world.getRows(); row++){
            for(int col=0; col<world.getColumns(); col++){
                final CellIndex index = new CellIndex(row, col);
                final CellContent content = world.getGrid().getCellAt(index).getCellContent();

                final double x = xOffset + col * cellWidth * scale;
                final double y = yOffset + row * cellHeight * scale;
                final double width = cellWidth * scale;
                final double height = cellHeight * scale;

                gc.setStroke(Color.rgb(60, 67, 78, 0.35));
                gc.strokeRect(x, y, width, height);

                if(content instanceof CellContent.Obstacle){
                    gc.setFill(Color.web("#59616d"));
                    gc.fillRect(x, y, width, height);
                } else if(content instanceof CellContent.Food food) {
                    final double size = Math.min(width, height) * 0.58;
                    gc.setFill(Color.web("#72d572"));
                    gc.fillOval(
                            x + (width - size) / 2.0,
                            y + (height - size) / 2.0,
                            size,
                            size
                    );

                    gc.setFill(Color.web("#102610"));
                    gc.fillText(
                            String.valueOf(food.quantity()),
                            x + width * 0.15,
                            y + height * 0.15
                    );
                } else if(content instanceof CellContent.Nest) {
                    gc.setFill(Color.web("#ad7650"));
                    gc.fillOval(
                            x + width * 0.15,
                            y + height * 0.15,
                            width * 0.70,
                            height * 0.70
                    );
                }
            }
        }
    }

    final void drawAnts(final GraphicsContext gc, final List<Ant> ants, final double scale, final double xOffset, final double yOffset){
        for(Ant ant : ants){
            final double x = xOffset + ant.getPosition().x() * scale;
            final double y = yOffset + ant.getPosition().y() * scale;
            final double size = Math.max(4.0, 7.0 * scale / 12.0);

            gc.save();
            gc.translate(x, y);
            gc.rotate(Math.toDegrees(ant.getAngle()));

            gc.setFill(colorFor(ant));
            gc.fillPolygon(
                    new double[] {size, -size, -size},
                    new double[] {0, -size * 0.65, size * 0.65},
                    3
            );

            if(ant.isCarryingFood()){
                gc.setFill(Color.web("#72d572"));
                gc.fillOval(-size * 0.4, -size * 0.4, size * 0.8, size * 0.8);
            }
            gc.restore();
        }
    }

    private Color colorFor(final Ant ant){
        if(ant.getState() == AntState.RETURNING_TO_NEST){
            return Color.web("#ff9f43");
        }
        if(ant.getRole() == AntRole.EXPLORER){
            return Color.web("#d980fa");
        }
        return Color.web("#f5f6fa");
    }

    private void editCellAt(final double screenX, final double screenY){
        if(renderWorld == null || renderScale <= 0){
            return;
        }

        final double worldX = (screenX - renderXOffset) / renderScale;
        final double worldY = (screenY - renderYOffset) / renderScale;

        if(worldX < 0 || worldX >= renderWorld.getWidth() || worldY < 0 || worldY >= renderWorld.getHeight()){
            return;
        }

        final CellIndex cell = renderWorld.convertToCellIndex(new WorldPosition(worldX, worldY));

        onWorldEdit.accept(new WorldEdit(cell, selectedTool, selectedFoodQuantity));
    }

    private ToggleButton createToolButton(final String text, final EditorTool tool, final ToggleGroup group){
        final ToggleButton button = new ToggleButton(text);
        button.setToggleGroup(group);
        button.setOnAction(event -> selectedTool = tool);
        return button;
    }

    private Spinner<Integer> createIntegerSpinner(final int min, final int max, final int initialValue){
        final Spinner<Integer> spinner = new Spinner<>(min, max, initialValue);

        spinner.setEditable(true);

        spinner.getEditor().setOnAction(event -> commitIntegerSpinner(spinner, min, max));
        spinner.getEditor().focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(!isFocused()){
                commitIntegerSpinner(spinner, min, max);
            }
        });

        return spinner;
    }

    private void commitIntegerSpinner(final Spinner<Integer> spinner, final int min, final int max){
        try {
            final int typedvalue = Integer.parseInt(spinner.getEditor().getText());
            spinner.getValueFactory().setValue(Math.clamp(typedvalue, min, max));
        } catch (NumberFormatException ignored) {
            spinner.getEditor().setText(String.valueOf(spinner.getValue()));
        }
    }
}
