package co.eci.snake.core;

import java.util.ArrayDeque;
import java.util.Deque;

public final class Snake {
    private final Deque<Position> body = new ArrayDeque<>();
    private volatile Direction direction;
    private int maxLength = 5;
    private boolean alive = true;

    /**
     * Crea una nueva serpiente en la posición inicial y con la dirección especificada.
     * @param start posición inicial de la serpiente.
     * @param dir dirección inicial de la serpiente.
     */
    private Snake(Position start, Direction dir) {
        body.addFirst(start);
        this.direction = dir;
    }

    /**
     * Crea una nueva serpiente en la posición (x, y) con la dirección inicial dir.
     * @param x posición x inicial de la serpiente.
     * @param y  posición y inicial de la serpiente.
     * @param dir dirección inicial de la serpiente.
     * @return una nueva serpiente.
     */
    public static Snake of(int x, int y, Direction dir) {
        return new Snake(new Position(x, y), dir);
    }

    /**
     * Obtiene la dirección actual de la serpiente.
     * @return la dirección actual de la serpiente.
     */
    public Direction direction() {
        return direction;
    }

    /**
     * Gira la serpiente en la dirección especificada, a menos que sea una dirección opuesta a la actual.
     * @param dir la nueva dirección para la serpiente.
     */
    public void turn(Direction dir) {
        if ((direction == Direction.UP && dir == Direction.DOWN) ||
                (direction == Direction.DOWN && dir == Direction.UP) ||
                (direction == Direction.LEFT && dir == Direction.RIGHT) ||
                (direction == Direction.RIGHT && dir == Direction.LEFT)) {
            return;
        }
        this.direction = dir;
    }

    /**
     * Obtiene la posición de la cabeza de la serpiente.
     * @return la posición de la cabeza de la serpiente.
     */
    public Position head() {
        return body.peekFirst();
    }

    /**
     * Obtiene una instantánea inmutable del cuerpo de la serpiente.
     * @return una instantánea inmutable del cuerpo de la serpiente.
     */
    public Deque<Position> snapshot() {
        return new ArrayDeque<>(body);
    }

    /**
     * Avanza la serpiente a una nueva posición.
     * @param newHead la nueva posición de la cabeza de la serpiente.
     * @param grow true si la serpiente debe crecer, false si no.
     */
    public void advance(Position newHead, boolean grow) {
        body.addFirst(newHead);
        if (grow)
            maxLength++;
        while (body.size() > maxLength)
            body.removeLast();
    }

    /**
     * Indica si la serpiente está viva.
     * @return true si la serpiente está viva, false si está muerta.
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * Establece el estado de vida de la serpiente.
     * @param alive true si la serpiente está viva, false si está muerta.
     */
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
