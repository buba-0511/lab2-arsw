package co.eci.snake.concurrency;

import co.eci.snake.core.Board;
import co.eci.snake.core.Direction;
import co.eci.snake.core.Snake;

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

    // Scheduler para ticks periódicos
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

    public SnakeRunner(Snake snake, Board board) {
        this.snake = snake;
        this.board = board;
        // Inicia el ciclo de ejecución al crear el objeto
        executor.schedule(this, baseSleepMs, TimeUnit.MILLISECONDS);
    }

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
        // Reprograma el siguiente tick
        executor.schedule(this, delay, TimeUnit.MILLISECONDS);
    }

    private void maybeTurn() {
        double p = (turboTicks > 0) ? 0.01 : 0.01;
        if (ThreadLocalRandom.current().nextDouble() < p) {
            randomTurn();
        }
    }

    private void randomTurn() {
        var dirs = Direction.values();
        snake.turn(dirs[ThreadLocalRandom.current().nextInt(dirs.length)]);
    }
}
