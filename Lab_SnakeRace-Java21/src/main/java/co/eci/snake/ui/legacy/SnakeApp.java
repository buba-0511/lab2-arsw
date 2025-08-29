package co.eci.snake.ui.legacy;

import co.eci.snake.concurrency.SnakeRunner;
import co.eci.snake.core.Board;
import co.eci.snake.core.Direction;
import co.eci.snake.core.Position;
import co.eci.snake.core.Snake;
import co.eci.snake.core.engine.GameClock;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public final class SnakeApp extends JFrame {

    private final Board board;
    private final GamePanel gamePanel;
    private static final JButton actionButton = new JButton();
    private final GameClock clock;
    private static final java.util.List<Snake> snakes = new java.util.ArrayList<>();
    private final java.util.List<SnakeRunner> runners = new java.util.ArrayList<>();
	private static final java.util.List<Snake> deathOrder = new java.util.ArrayList<>();

    public SnakeApp() {
        super("The Snake Race");
        this.board = new Board(35, 28);

        int N = Integer.getInteger("snakes", 2);
        for (int i = 0; i < N; i++) {
            int x = 2 + (i * 3) % board.width();
            int y = 2 + (i * 2) % board.height();
            var dir = Direction.values()[i % Direction.values().length];
            snakes.add(Snake.of(x, y, dir));
        }

        this.gamePanel = new GamePanel(board, () -> snakes);
        actionButton.setText("Iniciar");

        setLayout(new BorderLayout());
        add(gamePanel, BorderLayout.CENTER);
        add(actionButton, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        this.clock = new GameClock(60, () -> SwingUtilities.invokeLater(gamePanel::repaint));

        var exec = Executors.newVirtualThreadPerTaskExecutor();
        //snakes.forEach(s -> exec.submit(new SnakeRunner(s, board)));
        snakes.forEach(s -> {
            SnakeRunner r = new SnakeRunner(s, board);
            runners.add(r);
        });
        actionButton.addActionListener((ActionEvent e) -> togglePause());

        gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "pause");
        gamePanel.getActionMap().put("pause", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                togglePause();
            }
        });

        var player = snakes.get(0);
        InputMap im = gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = gamePanel.getActionMap();
        im.put(KeyStroke.getKeyStroke("LEFT"), "left");
        im.put(KeyStroke.getKeyStroke("RIGHT"), "right");
        im.put(KeyStroke.getKeyStroke("UP"), "up");
        im.put(KeyStroke.getKeyStroke("DOWN"), "down");
        am.put("left", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.turn(Direction.LEFT);
            }
        });
        am.put("right", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.turn(Direction.RIGHT);
            }
        });
        am.put("up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.turn(Direction.UP);
            }
        });
        am.put("down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.turn(Direction.DOWN);
            }
        });

        if (snakes.size() > 1) {
            var p2 = snakes.get(1);
            /**
             * im.put(KeyStroke.getKeyStroke('A'), "p2-left");
             * im.put(KeyStroke.getKeyStroke('D'), "p2-right");
             * im.put(KeyStroke.getKeyStroke('W'), "p2-up");
             * im.put(KeyStroke.getKeyStroke('S'), "p2-down");
             *
             */
            im.put(KeyStroke.getKeyStroke("pressed A"), "p2-left");
            im.put(KeyStroke.getKeyStroke("pressed D"), "p2-right");
            im.put(KeyStroke.getKeyStroke("pressed W"), "p2-up");
            im.put(KeyStroke.getKeyStroke("pressed S"), "p2-down");

            am.put("p2-left", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    p2.turn(Direction.LEFT);
                }
            });
            am.put("p2-right", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    p2.turn(Direction.RIGHT);
                }
            });
            am.put("p2-up", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    p2.turn(Direction.UP);
                }
            });
            am.put("p2-down", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    p2.turn(Direction.DOWN);
                }
            });
        }

        setVisible(true);
        clock.start();
    }

    /**
     * private void togglePause() { if
     * ("Iniciar".equals(actionButton.getText())) {
     * actionButton.setText("Resume"); clock.pause(); } else {
     * actionButton.setText("Iniciar"); clock.resume(); } }
    *
     */

    private void togglePause() {
        switch (actionButton.getText()) {
            case "Iniciar":
                actionButton.setText("Pausar");
                clock.start();
                runners.forEach(r -> r.setPaused(false));
                break;
            case "Pausar":
                actionButton.setText("Reanudar");
                clock.pause();
                runners.forEach(r -> r.setPaused(true));
                break;
            case "Reanudar":
                actionButton.setText("Pausar");
                clock.resume();
                runners.forEach(r -> r.setPaused(false));
                break;
        }
		gamePanel.repaint();
    }

	public static void launch() {
		SwingUtilities.invokeLater(SnakeApp::new);
	}
    
	public static void registerDeath(Snake s) {
        if (!deathOrder.contains(s)) {
            deathOrder.add(s);
        }
    }

	private static Snake getWorstSnake() {
        return deathOrder.isEmpty() ? null : deathOrder.get(0);
    }

	private static Snake getLongestAliveSnake() {
		Snake longest = null;

		for (Snake snake : snakes) {
			if (snake.isAlive()) {
				if (longest == null || snake.snapshot().size() > longest.snapshot().size()) {
					longest = snake;
				}
			}
		}

		return longest;
	}

    public static final class GamePanel extends JPanel {

        private final Board board;
        private final Supplier snakesSupplier;
        private final int cell = 20;

        @FunctionalInterface
        public interface Supplier {

            List<Snake> get();
        }

        public GamePanel(Board board, Supplier snakesSupplier) {
            this.board = board;
            this.snakesSupplier = snakesSupplier;
            setPreferredSize(new Dimension(board.width() * cell + 1, board.height() * cell + 40));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            var g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(220, 220, 220));
            for (int x = 0; x <= board.width(); x++) {
                g2.drawLine(x * cell, 0, x * cell, board.height() * cell);
            }
            for (int y = 0; y <= board.height(); y++) {
                g2.drawLine(0, y * cell, board.width() * cell, y * cell);
            }

            // Obstáculos
            g2.setColor(new Color(255, 102, 0));
            for (var p : board.obstacles()) {
                int x = p.x() * cell, y = p.y() * cell;
                g2.fillRect(x + 2, y + 2, cell - 4, cell - 4);
                g2.setColor(Color.RED);
                g2.drawLine(x + 4, y + 4, x + cell - 6, y + 4);
                g2.drawLine(x + 4, y + 8, x + cell - 6, y + 8);
                g2.drawLine(x + 4, y + 12, x + cell - 6, y + 12);
                g2.setColor(new Color(255, 102, 0));
            }

            // Ratones
            g2.setColor(Color.BLACK);
            for (var p : board.mice()) {
                int x = p.x() * cell, y = p.y() * cell;
                g2.fillOval(x + 4, y + 4, cell - 8, cell - 8);
                g2.setColor(Color.WHITE);
                g2.fillOval(x + 8, y + 8, cell - 16, cell - 16);
                g2.setColor(Color.BLACK);
            }

            // Teleports (flechas rojas)
            Map<Position, Position> tp = board.teleports();
            g2.setColor(Color.RED);
            for (var entry : tp.entrySet()) {
                Position from = entry.getKey();
                int x = from.x() * cell, y = from.y() * cell;
                int[] xs = {x + 4, x + cell - 4, x + cell - 10, x + cell - 10, x + 4};
                int[] ys = {y + cell / 2, y + cell / 2, y + 4, y + cell - 4, y + cell / 2};
                g2.fillPolygon(xs, ys, xs.length);
            }

            // Turbo (rayos)
            g2.setColor(Color.BLACK);
            for (var p : board.turbo()) {
                int x = p.x() * cell, y = p.y() * cell;
                int[] xs = {x + 8, x + 12, x + 10, x + 14, x + 6, x + 10};
                int[] ys = {y + 2, y + 2, y + 8, y + 8, y + 16, y + 10};
                g2.fillPolygon(xs, ys, xs.length);
            }

            // Serpientes
            var snakes = snakesSupplier.get();
            int idx = 0;
            for (Snake s : snakes) {
                var body = s.snapshot().toArray(new Position[0]);
                for (int i = 0; i < body.length; i++) {
                    var p = body[i];
                    Color base;
					if (idx == 0) {
						base = new Color(0, 170, 0); // p1
					} else if (idx == 1) {
						base = new Color(0, 0, 0); // p2
					} else {
						base = new Color(0, 160, 180); // otras
					}
                    int shade = Math.max(0, 40 - i * 4);
                    g2.setColor(new Color(
                            Math.min(255, base.getRed() + shade),
                            Math.min(255, base.getGreen() + shade),
                            Math.min(255, base.getBlue() + shade)));
                    g2.fillRect(p.x() * cell + 2, p.y() * cell + 2, cell - 4, cell - 4);
                }
                idx++;
            }

			if ("Reanudar".equals(actionButton.getText())) {
				Snake longest = getLongestAliveSnake();
				Snake worst   = getWorstSnake();

				java.util.List<String> mensajes = new java.util.ArrayList<>();
				if (worst == null) {
					mensajes.add("No ha muerto ninguna serpiente aún.");
				} else if (!worst.isAlive()) {
					mensajes.add("La peor serpiente fue de tamaño " + worst.snapshot().size());
				}
				if (longest != null) {
					mensajes.add("La serpiente viva más grande tiene tamaño: " + longest.snapshot().size());
				}

				if (!mensajes.isEmpty()) {
					g2.setFont(new Font("Arial", Font.BOLD, 20));
					FontMetrics fm = g2.getFontMetrics();


					int lineHeight = fm.getHeight();
					int padding = 20;
					int boxHeight = mensajes.size() * lineHeight + padding * 2;
					int boxWidth = mensajes.stream().mapToInt(fm::stringWidth).max().orElse(200) + padding * 2;

					int panelWidth = getWidth();
					int panelHeight = getHeight();
					int boxX = (panelWidth - boxWidth) / 2;
					int boxY = (panelHeight - boxHeight) / 2;

					g2.setColor(new Color(0, 0, 0, 180));
					g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);

					g2.setColor(Color.WHITE);
					g2.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);

					g2.setColor(Color.WHITE);
					int textY = boxY + padding + fm.getAscent();
					for (String msg : mensajes) {
						int textX = boxX + (boxWidth - fm.stringWidth(msg)) / 2;
						g2.drawString(msg, textX, textY);
						textY += lineHeight;
					}
				}
			}
			g2.dispose();
    	}
	}
}
