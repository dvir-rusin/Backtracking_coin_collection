public class Coin {
    int x, y;
    private boolean collected = false; // Collected state

    public Coin(int x, int y) {
        this.x = x;
        this.y = y;
    }
    // Method to mark the coin as collected
    public void collect() {
        this.collected = true;
    }

    // Check if the coin has been collected
    public boolean isCollected() {
        return collected;
    }
    // Getter and setter methods
}