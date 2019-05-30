package controller.tabs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import model.Global;
import model.ModelGameOfLife;
import model.cells.Cell;
import model.cells.CellGameOfLife;
import model.painters.PainterGameOfLife;

import java.net.URL;
import java.util.ResourceBundle;

public class ControllerGameOfLife implements Initializable {
    @FXML
    RadioButton radio10, radio20, radio50, radio100, radio200, radio500, radio1000;
    @FXML
    TextField randomCellsField;
    @FXML
    ChoiceBox<String> choiceBoxGridSize;
    @FXML
    Button startButton, pauseButton, stopButton, randomFillButton, resetButton;
    @FXML
    Canvas canvas2D;

    private ToggleGroup group = new ToggleGroup();
    private GraphicsContext gc;
    private ModelGameOfLife model;
    private int gridHeight, gridWidth;
    private PainterGameOfLife painterGameOfLife;
    private Thread thread;

    private void cleanCanvas() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas2D.getWidth(), canvas2D.getHeight());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String[] options = new String[]{"10x10", "20x20", "40x40", "50x50", "100x100", "200x200", "300x300", "600x600"};
        choiceBoxGridSize.setItems(FXCollections.observableArrayList(options));
        choiceBoxGridSize.setValue("10x10");
        gridHeight = 10;
        gridWidth = 10;

        //listener do choice boxa
        choiceBoxGridSize.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            final int value = newValue.intValue();

            switch (value) {
                case 0:
                    gridHeight = 10;
                    gridWidth = 10;
                    break;
                case 1:
                    gridHeight = 20;
                    gridWidth = 20;
                    break;
                case 2:
                    gridHeight = 40;
                    gridWidth = 40;
                    break;
                case 3:
                    gridHeight = 50;
                    gridWidth = 50;
                    break;
                case 4:
                    gridHeight = 100;
                    gridWidth = 100;
                    break;
                case 5:
                    gridHeight = 200;
                    gridWidth = 200;
                    break;
                case 6:
                    gridHeight = 300;
                    gridWidth = 300;
                    break;
                case 7:
                    gridHeight = 600;
                    gridWidth = 600;
                    break;
            }
            model = new ModelGameOfLife(gridHeight, gridWidth);
            reset(new ActionEvent());
        });

        pauseButton.setDisable(true);
        stopButton.setDisable(true);

        gc = canvas2D.getGraphicsContext2D();
        cleanCanvas();

        //dołączenie radioButtonów do grupy
        radio10.setToggleGroup(group);
        radio10.setSelected(true);
        radio20.setToggleGroup(group);
        radio50.setToggleGroup(group);
        radio100.setToggleGroup(group);
        radio200.setToggleGroup(group);
        radio500.setToggleGroup(group);
        radio1000.setToggleGroup(group);

        group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> Global.animationSpeedGameOfLife = setSpeed());

        //blokowanie wpisywanie wartości innych niż liczbowych
        randomCellsField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.matches("\\d*"))
                    randomCellsField.setText(oldValue);
            } catch (Exception ignored) {
            }
        });

        model = new ModelGameOfLife(gridHeight, gridWidth);
    }

    private int setSpeed(){
        int speed = 0;
        if (group.getSelectedToggle() == radio10) speed = 10;
        else if (group.getSelectedToggle() == radio20) speed = 20;
        else if (group.getSelectedToggle() == radio50) speed = 50;
        else if (group.getSelectedToggle() == radio100) speed = 100;
        else if (group.getSelectedToggle() == radio200) speed = 200;
        else if (group.getSelectedToggle() == radio500) speed = 500;
        else if (group.getSelectedToggle() == radio1000) speed = 1000;

        return speed;
    }

    public void start(ActionEvent actionEvent) {
        startButton.setDisable(true);
        pauseButton.setDisable(false);
        stopButton.setDisable(false);
        randomFillButton.setDisable(true);
        resetButton.setDisable(true);
        choiceBoxGridSize.setDisable(true);

        cleanCanvas();

        //uruchomienie Garbage Collectora
        System.gc();

        painterGameOfLife = new PainterGameOfLife(canvas2D, model, gc);
        thread = new Thread(painterGameOfLife);
        thread.setDaemon(true);
        thread.start();
    }

    public void pause(ActionEvent actionEvent) {
        startButton.setDisable(true);

        if (painterGameOfLife.isPaused())
            painterGameOfLife.resume();
        else
            painterGameOfLife.pause();
    }

    public void stop(ActionEvent actionEvent) {
        startButton.setDisable(false);
        pauseButton.setDisable(true);
        stopButton.setDisable(true);
        randomFillButton.setDisable(false);
        resetButton.setDisable(false);
        choiceBoxGridSize.setDisable(false);

        painterGameOfLife.stop();
    }

    public void gliderButton(ActionEvent actionEvent) {

        try {
            model = new ModelGameOfLife(gridHeight, gridWidth);
            model.clearGrid();
            model.fillGlider();

            int height = (int) canvas2D.getHeight() / model.getGridHeight();
            int width = (int) canvas2D.getWidth() / model.getGridWidth();
            Platform.runLater(() -> {
                Cell[][] tab = model.getGrid();
                cleanCanvas();
                gc.setFill(Color.BLACK);
                for (int i = 0; i < model.getGridHeight(); i++) {
                    for (int j = 0; j < model.getGridWidth(); j++)
                        if (tab[i][j].getState() == CellGameOfLife.State.ALIVE)
                            gc.fillRect(j * width, i * height, height, width);
                }
            });

        }
        catch (Exception ignored){}
    }
    public void staleButton(ActionEvent actionEvent) {

        try {
            model = new ModelGameOfLife(gridHeight, gridWidth);
            model.clearGrid();
            model.fillStale();

            int height = (int) canvas2D.getHeight() / model.getGridHeight();
            int width = (int) canvas2D.getWidth() / model.getGridWidth();
            Platform.runLater(() -> {
                Cell[][] tab = model.getGrid();
                cleanCanvas();
                gc.setFill(Color.BLACK);
                for (int i = 0; i < model.getGridHeight(); i++) {
                    for (int j = 0; j < model.getGridWidth(); j++)
                        if (tab[i][j].getState() == CellGameOfLife.State.ALIVE)
                            gc.fillRect(j * width, i * height, height, width);
                }
            });

        }
        catch (Exception ignored){}
    }
    public void oscylatorButton(ActionEvent actionEvent) {

        try {
            model = new ModelGameOfLife(gridHeight, gridWidth);
            model.clearGrid();
            model.fillOscylator();

            int height = (int) canvas2D.getHeight() / model.getGridHeight();
            int width = (int) canvas2D.getWidth() / model.getGridWidth();
            Platform.runLater(() -> {
                Cell[][] tab = model.getGrid();
                cleanCanvas();
                gc.setFill(Color.BLACK);
                for (int i = 0; i < model.getGridHeight(); i++) {
                    for (int j = 0; j < model.getGridWidth(); j++)
                        if (tab[i][j].getState() == CellGameOfLife.State.ALIVE)
                            gc.fillRect(j * width, i * height, height, width);
                }
            });
        }
        catch (Exception ignored){}
    }

    public void randomFill(ActionEvent actionEvent) {
        int amount;
        try {
            amount = Integer.parseInt(randomCellsField.getText());
            if (amount > 0) {
                model = new ModelGameOfLife(gridHeight, gridWidth);
                model.clearGrid();
                model.fillRandomly(amount);

                int height = (int) canvas2D.getHeight() / model.getGridHeight();
                int width = (int) canvas2D.getWidth() / model.getGridWidth();
                Platform.runLater(() -> {
                    Cell[][] tab = model.getGrid();
                    cleanCanvas();
                    gc.setFill(Color.BLACK);
                    for (int i = 0; i < model.getGridHeight(); i++) {
                        for (int j = 0; j < model.getGridWidth(); j++)
                            if (tab[i][j].getState() == CellGameOfLife.State.ALIVE)
                                gc.fillRect(j * width, i * height, height, width);
                    }
                });
            }
        }
        catch (Exception ignored){}
    }

    public void reset(ActionEvent actionEvent) {
        cleanCanvas();
        model.clearGrid();
    }

    private void drawOnCanvas(MouseEvent mouseEvent, boolean type){
        int x0 = 10, y0 = 39; //współrzędne początka canvasa w okienku
        int x = (int)mouseEvent.getSceneX() - x0, y = (int)mouseEvent.getSceneY() - y0; //współrzędne w okienku

        //rozmiary komórki
        int height = (int) canvas2D.getHeight() / model.getGridHeight();
        int width = (int) canvas2D.getWidth() / model.getGridWidth();

        //pozycja komórki w oknie
        int canvasX = (x/height)*height;
        int canvasY = (y/width)*width;

        //pozycja komórki w siatce
        int gridX = canvasX/height;
        int gridY = canvasY/width;

        if (gridX > model.getGridWidth()-1)
            gridX = model.getGridWidth()-1;
        if (gridX < 0)
            gridX = 0;

        if (gridY > model.getGridHeight()-1)
            gridY = model.getGridHeight()-1;
        if (gridY < 0)
            gridY = 0;


        int finalGridY = gridY;
        int finalGridX = gridX;
        Platform.runLater(() -> {
            byte state = model.getGrid()[finalGridY][finalGridX].getState();

            if (state == CellGameOfLife.State.DEAD || type) {
                model.getGrid()[finalGridY][finalGridX].setState(CellGameOfLife.State.ALIVE);
                gc.setFill(Color.BLACK);
                gc.fillRect(canvasX, canvasY, height, width);
            }
            else if (state == CellGameOfLife.State.ALIVE) {
                model.getGrid()[finalGridY][finalGridX].setState(CellGameOfLife.State.DEAD);
                gc.setFill(Color.WHITE);
                gc.fillRect(canvasX, canvasY, height, width);
            }
        });

    }

    public void mouseClick(MouseEvent mouseEvent) {
        drawOnCanvas(mouseEvent, false);
    }

    public void mouseDrag(MouseEvent mouseEvent) {
        drawOnCanvas(mouseEvent, true);
    }
}