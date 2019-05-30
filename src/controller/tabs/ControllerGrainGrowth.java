package controller.tabs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import model.Global;

import static model.ModelGrainGrowth.*;

import model.ModelGrainGrowth;
import model.cells.CellGrain;
import model.painters.PainterGrainGrowth;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static model.ModelGrainGrowth.EdgeType.*;
import static model.ModelGrainGrowth.NeighborhoodType.*;
import static model.ModelGrainGrowth.NeighborhoodType.randomHexagonal;
import static model.ModelGrainGrowth.TypeOfPlacement.*;
import static model.ModelGrainGrowth.TypeOfPlacement.MonteCarlo;

public class ControllerGrainGrowth implements Initializable {
    @FXML
    ScrollPane scrollPane;
    @FXML
    AnchorPane pane;
    @FXML
    Label labelGridHeight, labelGridWidth, labelAnimationSpeed, labelGridSize;
    @FXML
    Slider sliderGridHeight, sliderGridWidth, sliderAnimationSpeed;
    @FXML
    TextField numberOfCellsField, radiusField;
    @FXML
    ChoiceBox<String> choiceBoxNeighborhoodType;
    @FXML
    ChoiceBox<String> choiceBoxEdgeType;
    @FXML
    ChoiceBox<String> choiceTypeOfPlacement;
    @FXML
    Button startButton, pauseButton, stopButton, fillButton, resetButton, smoothingMonteCarloButtonStart;
    @FXML
    Canvas canvas2D;

    private GraphicsContext gc;
    private ModelGrainGrowth model;
    private int gridHeight, gridWidth;
    private PainterGrainGrowth painter;
    private Thread thread;
    private NeighborhoodType nType;
    private EdgeType eType;
    private TypeOfPlacement pType;
    private int grainHeight, grainWidth;

    private void cleanCanvas() {
        gc.setFill(Color.WHITE);
        gc.clearRect(0, 0, canvas2D.getHeight(), canvas2D.getWidth());
        if (model != null) gc.fillRect(0, 0, model.getGridWidth() * grainHeight, model.getGridHeight() * grainWidth);
        else gc.fillRect(0, 0, canvas2D.getHeight() * grainHeight, canvas2D.getWidth() * grainWidth);
    }

    private void displayAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = canvas2D.getGraphicsContext2D();

        String[] neighboursOptions = new String[]{"von Neuman", "Moore", "Pentagonal left", "Pentagonal right", "Pentagonal up", "Pentagonal down", "Pentagonal random", "Hexagonal left", "Hexagonal right", "Hexagonal random"};
        choiceBoxNeighborhoodType.setItems(FXCollections.observableArrayList(neighboursOptions));
        choiceBoxNeighborhoodType.setValue("von Neuman");
        nType = NeighborhoodType.vonNeuman;
        choiceBoxNeighborhoodType.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue.intValue()) {
                case 0:
                    nType = vonNeuman;
                    break;
                case 1:
                    nType = Moore;
                    break;
                case 2:
                    nType = leftPentagonal;
                    break;
                case 3:
                    nType = rightPentagonal;
                    break;
                case 4:
                    nType = upPentagonal;
                    break;
                case 5:
                    nType = downPentagonal;
                    break;
                case 6:
                    nType = randomPentagonal;
                    break;
                case 7:
                    nType = leftHexagonal;
                    break;
                case 8:
                    nType = rightHexagonal;
                    break;
                case 9:
                    nType = randomHexagonal;
                    break;
            }

            if (model != null)
                model.setNeighborhoodType(nType);
        });

        String[] edgeOptions = new String[]{"Closed", "Periodic"};
        choiceBoxEdgeType.setItems(FXCollections.observableArrayList(edgeOptions));
        choiceBoxEdgeType.setValue("Closed");
        eType = EdgeType.Closed;
        choiceBoxEdgeType.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue.intValue()) {
                case 0:
                    eType = Closed;
                    break;
                case 1:
                    eType = Periodic;
                    break;
            }
            model = new ModelGrainGrowth(gridHeight, gridWidth, nType, eType);
            cleanCanvas();
        });

        String[] placementOptions = new String[]{"Random", "Evenly placement", "Placement with radius", "Monte Carlo"};
        choiceTypeOfPlacement.setItems(FXCollections.observableArrayList(placementOptions));
        choiceTypeOfPlacement.setValue("Random");
        pType = Random;
        choiceTypeOfPlacement.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue.intValue()) {
                case 0:
                    pType = Random;
                    radiusField.setDisable(true);
                    break;
                case 1:
                    pType = EvenlyPlacement;
                    radiusField.setDisable(false);
                    break;
                case 2:
                    pType = RandomWithRadius;
                    radiusField.setDisable(false);
                    break;
                case 3:
                    pType = MonteCarlo;
                    radiusField.setDisable(true);
                    break;
            }
            if (model != null) {
                model.setPlacementType(pType);
                Global.placementType = pType;
            }
        });

        //blokowanie wpisywanie wartości innych niż liczbowych
        numberOfCellsField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.matches("\\d*"))
                    numberOfCellsField.setText(oldValue);
            } catch (Exception ignored) {
            }
        });
        radiusField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.matches("\\d*")) {
                    radiusField.setText(oldValue);
                }
            } catch (Exception ignored) {
            }
        });

        sliderGridHeight.setMin(1);
        sliderGridHeight.setMax(canvas2D.getHeight());

        sliderGridWidth.setMin(1);
        sliderGridWidth.setMax(canvas2D.getWidth());

        sliderAnimationSpeed.setMin(10);
        sliderAnimationSpeed.setMax(1000);
        sliderAnimationSpeed.setValue(10);

        grainHeight = 1;
        grainWidth = 1;
        gridWidth = (int) canvas2D.getHeight() / grainHeight;
        gridHeight = (int) canvas2D.getWidth() / grainWidth;
        model = new ModelGrainGrowth(gridHeight, gridWidth, nType, eType);
        model.setPlacementType(pType);
        cleanCanvas();

        pauseButton.setDisable(true);
        stopButton.setDisable(true);
        radiusField.setDisable(true);
        smoothingMonteCarloButtonStart.setDisable(true);
    }

    private void setLabelGridSize() {
        labelGridSize.setText("Grid size: " + gridHeight + "x" + gridWidth);
    }

    public void setGridHeight(MouseEvent mouseEvent) {
        grainWidth = (int) sliderGridHeight.getValue();
        Global.grainWidth = grainWidth;
        labelGridHeight.setText(Integer.toString(grainWidth));
        setLabelGridSize();
    }

    public void gridHeightPressed(KeyEvent keyEvent) {
        grainWidth = (int) sliderGridHeight.getValue();
        gridHeight = (int) canvas2D.getWidth() / grainWidth;
        Global.grainWidth = grainWidth;
        labelGridHeight.setText(Integer.toString(grainWidth));
        setLabelGridSize();
        if (model != null) {
            model.setGridHeight(gridHeight);
            model.createGrid();
        }
    }

    public void setGridWidth(MouseEvent mouseEvent) {
        grainHeight = (int) sliderGridWidth.getValue();
        Global.grainHeight = grainHeight;
        labelGridWidth.setText(Integer.toString(grainHeight));
        setLabelGridSize();
    }

    public void gridWidthPressed(KeyEvent keyEvent) {
        grainHeight = (int) sliderGridWidth.getValue();
        gridWidth = (int) canvas2D.getHeight() / grainHeight;
        Global.grainHeight = grainHeight;
        labelGridWidth.setText(Integer.toString(grainHeight));
        setLabelGridSize();
        if (model != null) {
            model.setGridWidth(gridWidth);
            model.createGrid();
        }
    }

    public void heightDone(MouseEvent dragEvent) {
        gridHeight = (int) canvas2D.getWidth() / grainWidth;
        if (model != null) {
            model.setGridHeight(gridHeight);
            model.createGrid();
        }
    }

    public void widthDone(MouseEvent dragEvent) {
        gridWidth = (int) canvas2D.getHeight() / grainHeight;
        if (model != null) {
            model.setGridWidth(gridWidth);
            model.createGrid();
        }
    }

    public void setAnimationSpeed(MouseEvent mouseEvent) {
        Global.animationSpeedGrainGrowth = (int) sliderAnimationSpeed.getValue();
        labelAnimationSpeed.setText(Integer.toString((int) sliderAnimationSpeed.getValue()));
    }

    public void animationSpeedPressed(KeyEvent keyEvent) {
        Global.animationSpeedGrainGrowth = (int) sliderAnimationSpeed.getValue();
        labelAnimationSpeed.setText(Integer.toString((int) sliderAnimationSpeed.getValue()));
    }

    private void refreshCanvas() {
        Platform.runLater(() -> {
            CellGrain[][] tab;

            tab = model.getGrid();
            cleanCanvas();

            int height = Global.grainHeight;
            int width = Global.grainWidth;
            for (int i = 0; i < model.getGridHeight(); i++) {
                for (int j = 0; j < model.getGridWidth(); j++) {
                    if (tab[i][j].getState() == CellGrain.State.GRAIN) {
                        gc.setFill(((ModelGrainGrowth.GrainType) model.getListOfGrains().get(tab[i][j].getId() - 1)).getGrainColor());
                        gc.fillRect(j * height, i * width, height, width);
                    }
                }
            }
        });
    }

    public void fillCanvas(ActionEvent actionEvent) {
        int grainAmount, distance_radius;
        try {
            grainAmount = Integer.parseInt(numberOfCellsField.getText());
        } catch (NumberFormatException e) {
            grainAmount = 1;
        }
        try {
            distance_radius = Integer.parseInt(radiusField.getText());
            if (distance_radius < 1) distance_radius = 1;
        } catch (NumberFormatException e) {
            distance_radius = 1;
        }

        if (model != null) {
            int numberOfAddedGrains;

            if (pType == Random) {
                numberOfAddedGrains = model.fillRandomly(grainAmount);
                refreshCanvas();
                if (numberOfAddedGrains < grainAmount)
                    displayAlert("Nie ma wystarczająco dużo miejsca aby dodać " + grainAmount + " dodatkowych ziaren." +
                            "\nUdało dodać się " + numberOfAddedGrains + " ziaren");
            } else if (pType == EvenlyPlacement) {
                numberOfAddedGrains = model.fillEvenlyPlacement(grainAmount, distance_radius);
                refreshCanvas();
                if (numberOfAddedGrains < grainAmount)
                    displayAlert("Nie ma wystarczająco dużo miejsca aby dodać " + grainAmount +
                            " dodatkowych ziaren, przy odległości między ziarnami równej " + distance_radius +
                            " i rozmiarze ziaren równym " + grainHeight + "x" + grainWidth +
                            "\nUdało dodać się " + numberOfAddedGrains + " ziaren");
            } else if (pType == RandomWithRadius) {
                numberOfAddedGrains = model.fillRandomlyWithRadius(grainAmount, distance_radius);
                refreshCanvas();
                if (numberOfAddedGrains < grainAmount)
                    displayAlert("Nie udało dodać się " + grainAmount + " ziaren." +
                            "\nDodano " + numberOfAddedGrains + " ziaren.");
            } else if (pType == MonteCarlo) {
                model.fillMonteCarlo(grainAmount);
                refreshCanvas();
            }
        }
    }

    @FXML
    private void addGrainOnCanvas(MouseEvent mouseEvent) {
        int x0 = 1, y0 = 30; //współrzędne początka canvasa w okienku
        //współrzędne w okienku
        int x = (int) mouseEvent.getSceneX() - x0 + ((int) scrollPane.getHvalue() + (int) scrollPane.getHvalue() / 26);
        int y = (int) mouseEvent.getSceneY() - y0 + ((int) scrollPane.getVvalue() + (int) scrollPane.getVvalue() / 26);

        //rozmiary komórki
        int height = grainHeight;
        int width = grainWidth;

        //pozycja komórki w oknie
        int canvasX = (x / height) * height;
        int canvasY = (y / width) * width;

        //pozycja komórki w siatce
        int gridX = canvasX / height;
        int gridY = canvasY / width;

        if (!(gridX > model.getGridWidth() - 1 || gridX < 0 || gridY > model.getGridHeight() - 1 || gridY < 0)) {
            model.addSingleGrain(gridY, gridX);
            refreshCanvas();
        }
    }

    public void reset(ActionEvent actionEvent) {
        model.reset();
        cleanCanvas();
        smoothingMonteCarloButtonStart.setDisable(true);
    }

    public void start(ActionEvent actionEvent) {
        startButton.setDisable(true);
        pauseButton.setDisable(false);
        stopButton.setDisable(false);
        choiceBoxEdgeType.setDisable(true);
        sliderGridHeight.setDisable(true);
        sliderGridWidth.setDisable(true);
        smoothingMonteCarloButtonStart.setDisable(true);

        //uruchomienie Garbage Collectora
        System.gc();

        painter = new PainterGrainGrowth(canvas2D, model, gc, this);
        thread = new Thread(painter);
        thread.setDaemon(true);
        thread.start();
    }

    public void pause(ActionEvent actionEvent) {
        startButton.setDisable(true);

        if (painter.isPaused())
            painter.resume();
        else
            painter.pause();
    }

    public void stop(ActionEvent actionEvent) {
        startButton.setDisable(false);
        pauseButton.setDisable(true);
        stopButton.setDisable(true);
        choiceBoxEdgeType.setDisable(false);
        sliderGridHeight.setDisable(false);
        sliderGridWidth.setDisable(false);
        smoothingMonteCarloButtonStart.setDisable(false);

        model.setPlacementType(pType);
        Global.placementType = pType;

        painter.stop();
    }

    public void resetButtons() {
        stop(new ActionEvent());
        smoothingMonteCarloButtonStart.setDisable(false);
    }

    public void saveToFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showSaveDialog(Global.primaryStage);

        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage((int) canvas2D.getWidth(), (int) canvas2D.getHeight());
                canvas2D.snapshot(null, writableImage);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(renderedImage, "png", file);
            } catch (IOException ex) {
            }
        }
    }

    public void smoothingMonteCarloButtonStart(ActionEvent actionEvent) {
        model.setPlacementType(MonteCarlo);
        Global.placementType = MonteCarlo;
        start(new ActionEvent());
    }
}
