package controller.tabs;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import model.Model1D;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller1D implements Initializable {
    @FXML
    ColorPicker backgroundColorPicker, sqareColorPicker;
    @FXML
    Slider sliderCellSize, sliderRuleNumber;
    @FXML
    TextField gridField, ruleField;
    @FXML
    Button drawButton;
    @FXML
    Canvas canvas1D;
    @FXML
    RadioButton radioNormal, radioPeriodic;

    private ToggleGroup group = new ToggleGroup(); //grupuje radio buttony
    private GraphicsContext gc;
    private Model1D rule;
    private int cellsInGrid, ruleNumber, type;
    private Color background, square;

    //czyszczenie canvasa
    private void cleanCanvas() {
        background = backgroundColorPicker.getValue();
        gc.setFill(background);
        gc.fillRect(0, 0, canvas1D.getWidth(), canvas1D.getHeight());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cellsInGrid = 2;
        ruleNumber = 0;
        type = Model1D.Type.NORMAL;

        sliderCellSize.setMin(2);
        sliderCellSize.setMax(600);

        sliderRuleNumber.setMin(0);
        sliderRuleNumber.setMax(255);

        radioNormal.setToggleGroup(group);
        radioNormal.setSelected(true);
        radioPeriodic.setToggleGroup(group);

        sqareColorPicker.setValue(Color.BLACK);

        gc = canvas1D.getGraphicsContext2D();
        cleanCanvas();

        //blokowanie wpisywanie wartości innych niż liczbowych
        gridField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.matches("\\d*"))
                    gridField.setText(oldValue);
            } catch (Exception ignored) {
            }
        });

        ruleField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.matches("\\d*"))
                    ruleField.setText(oldValue);
            } catch (Exception ignored) {
            }
        });
    }

    private void setGridWidth() {
        try {
            cellsInGrid = Integer.parseInt(gridField.getText());
            if (cellsInGrid > 600) //bo 600 to max siatki
                cellsInGrid = 600;
            if (cellsInGrid < 2)
                cellsInGrid = 2;
        } catch (Exception ignored) {
        }
    }

    private void setRule() {
        try {
            int tmp = Integer.parseInt(ruleField.getText());
            if (tmp >= 0 && tmp <= 255)
                ruleNumber = tmp;
        } catch (Exception ignored) {
        }
    }

    public void draw(ActionEvent actionEvent) {
        setGridWidth();
        setRule();
        sliderCellSize.setValue(cellsInGrid);
        sliderRuleNumber.setValue(ruleNumber);
        cleanCanvas();

        square = sqareColorPicker.getValue();
        gc.setFill(square);

        int size = (int) (canvas1D.getWidth() / cellsInGrid);

        if (group.getSelectedToggle() == radioNormal) type = Model1D.Type.NORMAL;
        else if (group.getSelectedToggle() == radioPeriodic) type = Model1D.Type.PERIODIC;

        rule = new Model1D(cellsInGrid, ruleNumber, type);

        final int[][] tab = {rule.getGrid()};
        Platform.runLater(() -> {
            for (int i = 0; i < cellsInGrid; i++) {
                for (int j = 0; j < cellsInGrid; j++)
                    if (tab[0][j] == Model1D.State.ALIVE)
                        gc.fillRect(j * size, i * size, size, size);
                tab[0] = rule.getResult(rule.getGrid());
            }
        });
    }

    public void sliderSetCell(MouseEvent mouseEvent) {
        cellsInGrid = (int) sliderCellSize.getValue();
        if (((int) canvas1D.getWidth()) % cellsInGrid == 0)
            gridField.setText(Integer.toString(cellsInGrid));
    }

    public void sliderSetRule(MouseEvent mouseEvent) {
        ruleNumber = (int) sliderRuleNumber.getValue();
        ruleField.setText(Integer.toString(ruleNumber));
    }

}
