package co.eci.snake.concurrency;

import co.eci.snake.core.Board;
import co.eci.snake.core.Direction;
import co.eci.snake.core.Snake;
import co.eci.snake.ui.legacy.SnakeApp;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public final class SnakeRunner implements Runnable {

    private final Snake snake;
    private final Board board;
    private final int baseSleepMs = 120; //80
    private final int turboSleepMs = 80; //40
    private int turboTicks = 0;

    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
    private volatile boolean paused = true;

    /**
     * Crea un corredor para una serpiente en un tablero.
     * @param snake la serpiente a controlar.
     * @param board el tablero donde se mueve la serpiente.
     */
    public SnakeRunner(Snake snake, Board board) {
        this.snake = snake;
        this.board = board;
        
        executor.schedule(this, baseSleepMs, TimeUnit.MILLISECONDS);
    }

    /**
    @Override
    public void run() {
        maybeTurn();
        var res = board.step(snake);
        if (res == Board.MoveResult.HIT_OBSTACLE) {
            randomTurn();
        } else if (res == Board.MoveResult.ATE_TURBO) {
            turboTicks = 100;
        }
        if (turboTicks > 0) {
            turboTicks--;
        }
        int delay = (turboTicks > 0) ? turboSleepMs : baseSleepMs;
        
        executor.schedule(this, delay, TimeUnit.MILLISECONDS);
    }

    
    **/ 
    
    /**
     * Pausa o reanuda la serpiente.
     * @param paused true para pausar, false para reanudar.
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }
    
    /**
     * Ejecuta un paso del juego para esta serpiente.
     * Si la serpiente está pausada o muerta, no hace nada.
     */
    @Override
    public void run() {
        if (!paused && snake.isAlive()) { 
            maybeTurn();
            var res = board.step(snake);
            if (res == Board.MoveResult.HIT_OBSTACLE) {
                snake.setAlive(false);
                SnakeApp.registerDeath(snake);
                return;
            } else if (res == Board.MoveResult.ATE_TURBO) {
                turboTicks = 100;
            }
            if (turboTicks > 0) turboTicks--;
        }
        int delay = (turboTicks > 0) ? turboSleepMs : baseSleepMs;
        executor.schedule(this, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Gira la serpiente con una probabilidad p.
     * Si la serpiente tiene turbo, p es mayor.
     */
    private void maybeTurn() {
        double p = (turboTicks > 0) ? 0.01 : 0.01;
        if (ThreadLocalRandom.current().nextDouble() < p) {
            randomTurn();
        }
    }

    /**
     * Gira la serpiente en una dirección aleatoria.
     */
    private void randomTurn() {
        var dirs = Direction.values();
        snake.turn(dirs[ThreadLocalRandom.current().nextInt(dirs.length)]);
    }
}
