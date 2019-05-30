package model.painters;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.Global;
import model.ModelGameOfLife;
import model.cells.Cell;

import java.util.concurrent.TimeUnit;

import static model.cells.CellGameOfLife.State;

public class PainterGameOfLife implements Runnable {
    private Canvas canvas2D;
    private ModelGameOfLife model;
    private GraphicsContext gc;
    private volatile boolean running;
    private volatile boolean paused;
    private final Object pauseLock = new Object();

    public PainterGameOfLife(Canvas canvas2D, ModelGameOfLife model, GraphicsContext gc) {
        this.canvas2D = canvas2D;
        this.model = model;
        this.gc = gc;
        running = true;
        paused = false;
    }

    private void cleanCanvas() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas2D.getWidth(), canvas2D.getHeight());
    }

    private void paint(){
        int height = (int) canvas2D.getHeight() / model.getGridHeight();
        int width = (int) canvas2D.getWidth() / model.getGridWidth();
        try {
            Platform.runLater(() -> {
                Cell[][] tab = model.getResult(model.getGrid());
                cleanCanvas();
                gc.setFill(Color.BLACK);
                for (int i = 0; i < model.getGridHeight(); i++) {
                    for (int j = 0; j < model.getGridWidth(); j++)
                        if (tab[i][j].getState() == State.ALIVE)
                            gc.fillRect(j * width, i * height, height, width);
                }
            });

            TimeUnit.MILLISECONDS.sleep(Global.animationSpeedGameOfLife);
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