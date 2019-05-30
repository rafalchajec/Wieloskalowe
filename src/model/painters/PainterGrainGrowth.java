package model.painters;

import controller.tabs.ControllerGrainGrowth;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.Global;
import model.ModelGrainGrowth;
import model.cells.CellGrain;

import java.util.concurrent.TimeUnit;

import static model.ModelGrainGrowth.TypeOfPlacement.MonteCarlo;
import static model.cells.CellGrain.State;

public class PainterGrainGrowth implements Runnable {
    private Canvas canvas2D;
    private ModelGrainGrowth model;
    private GraphicsContext gc;
    private volatile boolean running;
    private volatile boolean paused;
    private final Object pauseLock = new Object();
    private ControllerGrainGrowth cgg;

    public PainterGrainGrowth(Canvas canvas2D, ModelGrainGrowth model, GraphicsContext gc, ControllerGrainGrowth controllerGrainGrowth) {
        this.canvas2D = canvas2D;
        this.model = model;
        this.gc = gc;
        running = true;
        paused = false;
        cgg = controllerGrainGrowth;
    }

    private void cleanCanvas() {
        gc.setFill(Color.WHITE);
        if (model != null) gc.fillRect(0, 0, model.getGridWidth() * Global.grainHeight, model.getGridHeight() * Global.grainWidth);
        else gc.fillRect(0, 0, canvas2D.getHeight() * Global.grainHeight, canvas2D.getWidth() * Global.grainWidth);
    }

    private int getNumberOfEmptyGrains(CellGrain[][] tab) {
        return model.getNumberOfEmptyGrains();
    }

    private void paint() {
        int height = Global.grainHeight;
        int width = Global.grainWidth;
        try {
            Platform.runLater(() -> {
                CellGrain[][] tab = model.getResult(model.getGrid());

                if (getNumberOfEmptyGrains(tab) == 0 && Global.placementType != MonteCarlo) {
                    stop();
                    cgg.resetButtons();
                    model.setGridEnergy(model.getGrid());
                }
                cleanCanvas();

                for (int i = 0; i < model.getGridHeight(); i++) {
                    for (int j = 0; j < model.getGridWidth(); j++) {
                        if (tab[i][j].getState() == State.GRAIN) {
                            gc.setFill(((ModelGrainGrowth.GrainType) model.getListOfGrains().get(tab[i][j].getId() - 1)).getGrainColor());
                            gc.fillRect(j * height, i * width, height, width);
                        }
                    }
                }
            });

            TimeUnit.MILLISECONDS.sleep(Global.animationSpeedGrainGrowth);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void run() {
        while (running) {
            synchronized (pauseLock) {
                if (!running)
                    break;
                if (paused) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException ex) {
                        break;
                    }
                    if (!running)
                        break;
                }
            }

            paint();
        }
    }

    public void stop() {
        running = false;
        resume();
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

    public boolean isPaused() {
        return paused;
    }
}
