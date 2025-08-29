package co.eci.snake.core;

public record Position(int x, int y) {

	/**
	 * Devuelve una nueva posición que es el resultado de aplicar el wrapping a esta posición dentro de un tablero de las dimensiones especificadas.
	 * @param width el ancho del tablero.
	 * @param height la altura del tablero.
	 * @return una nueva posición envuelta dentro de las dimensiones del tablero.
	 */
	public Position wrap(int width, int height) {
		int nx = ((x % width) + width) % width;
		int ny = ((y % height) + height) % height;
		return new Position(nx, ny);
	}
}
