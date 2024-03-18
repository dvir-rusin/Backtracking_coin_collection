import javax.swing.JPanel;
import java.awt.*;
import javax.swing.Timer;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;

public class GamePanel extends JPanel {
    private int width, height;
    private GameEngine gameEngine;
    private long startTime;
    private long elapsedTime = 0;
    private javax.swing.Timer timer;

    private int botDelay=50;

    public GamePanel(int width, int height) {
        this.width = width;
        this.height = height;
        this.gameEngine = new GameEngine(5,10,width,height); // Example: Initialize with 10 bots and coins

        // Initialize and start the game timer
        startTime = System.currentTimeMillis();
        timer = new javax.swing.Timer(botDelay, e -> { // Consider updating more frequently for a smooth timer
            gameEngine.updateBotPositions(); // Assuming this method moves the bots
            gameEngine.updateCoinCollections(); // Check and update coin collections
            // Trigger the backtracking optimization from here or within GamePanel after setup
            //gameEngine.optimizeAssignments();

            if (!gameEngine.allCoinsCollected()) {
                elapsedTime = System.currentTimeMillis() - startTime;
                repaint(); // Update the timer display
            } else {
                timer.stop(); // Stop the timer
                System.out.println("All coins collected in: " + formatTime(elapsedTime));

            }
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Example for drawing a more appealing bot
        for (Bot bot : gameEngine.bots) {

            g2d.setColor(Color.black); // Bot color
            g2d.fillOval(bot.x - 10, bot.y - 10, 40, 40); // Bot body
            g2d.setColor(Color.white); // Eyes
            g2d.fillOval(bot.x - 5, bot.y - 5, 5, 5);
            g2d.fillOval(bot.x + 2, bot.y - 5, 5, 5);
        }

        // Example for drawing a more appealing coin

        for (Coin coin : gameEngine.coins) {
            if (!coin.isCollected()) {
                g2d.setColor(Color.YELLOW); // Coin color
                g2d.fillOval(coin.x - 10, coin.y - 10, 40, 40); // Coin shape
                g2d.setColor(Color.BLACK); // Coin marking
                g2d.drawString("$", coin.x - 5, coin.y + 5);
            }

        }
        // Example for drawing a more visually appealing wall
        Stroke originalStroke = g2d.getStroke(); // Preserve the original stroke
        g2d.setColor(Color.DARK_GRAY); // Wall color
        g2d.setStroke(new BasicStroke(5)); // Set the stroke width to make the wall look thicker

        for(Wall wall: gameEngine.walls){
            g.drawLine(wall.startX, wall.startY, wall.endX, wall.endY);
        }
        // Restore the original stroke
        g2d.setStroke(originalStroke);


        // Draw lines between bots and their assigned coins
        for (int i = 0; i < gameEngine.bots.length; i++) {
            Bot bot = gameEngine.bots[i];
            Coin targetCoin = gameEngine.getAssignedCoin(i);
            if (targetCoin != null) {
                g.drawLine(bot.x, bot.y, targetCoin.x, targetCoin.y);
            }
        }

        // Draw the timer at the top of the screen
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        // Drawing bots, coins, etc., plus the timer text at the top
        g.drawString("Time: " + formatTime(elapsedTime), 10, 20);
        if (gameEngine.allCoinsCollected()){
            g.drawString("All 10 coins were collected", 350, 20);
        }
    }

    private String formatTime(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void startSimulation() {
        // Call the method to find the optimal assignments of bots to coins.
        gameEngine.optimizeAssignments();

        // Assuming optimizeAssignments updates bestAssignment and prepares for animation
        // Now, start the animation timer to update bot positions and repaint the panel
        //botDelay=50; // Milliseconds between updates
        new Timer(botDelay, e -> {
            gameEngine.updateBotPositions();
            repaint(); // Repaint to reflect position updates
        }).start();
    }


}