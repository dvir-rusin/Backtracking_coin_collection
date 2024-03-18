import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Bot Coin Collector");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            GamePanel gamePanel = new GamePanel(800, 600);
            frame.add(gamePanel);

            frame.setVisible(true);

            gamePanel.startSimulation();
        });
    }
}