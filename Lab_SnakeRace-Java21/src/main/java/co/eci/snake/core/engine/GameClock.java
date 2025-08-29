package co.eci.snake.core.engine;

import co.eci.snake.core.GameState;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class GameClock implements AutoCloseable {
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private final long periodMillis;
	private final Runnable tick;
	private final java.util.concurrent.atomic.AtomicReference<GameState> state = new AtomicReference<>(GameState.STOPPED);
	private ScheduledFuture<?> future;

	/**
	 * Crea un reloj de juego que ejecuta una tarea periódicamente.
	 * @param periodMillis el período en milisegundos entre ejecuciones.
	 * @param tick la tarea a ejecutar periódicamente.
	 */
	public GameClock(long periodMillis, Runnable tick) {
		if (periodMillis <= 0)
			throw new IllegalArgumentException("periodMillis must be > 0");
		this.periodMillis = periodMillis;
		this.tick = java.util.Objects.requireNonNull(tick, "tick");
	}

	/**
	 * Inicia el reloj si está detenido.
	 */
	public void start() {
		if (state.compareAndSet(GameState.STOPPED, GameState.RUNNING)) {
			scheduler.scheduleAtFixedRate(() -> {
				if (state.get() == GameState.RUNNING)
					tick.run();
			}, 0, periodMillis, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * Pausa el reloj si está en ejecución.
	 */
	public void pause() {
		state.set(GameState.PAUSED);
	}

	/**
	 * Reanuda el reloj si está pausado.
	 */
	public void resume() {
		state.set(GameState.RUNNING);
	}

	/**
	 * Detiene el reloj si está en ejecución.
	 */
    public void stop() {
        state.set(GameState.STOPPED);
        if (future != null) {
            future.cancel(false);
            future = null;
        }
    }
	
	/**
	 * Cierra el reloj y detiene todas las tareas programadas.
	 */
	@Override
	public void close() {
		scheduler.shutdownNow();
	}
}
