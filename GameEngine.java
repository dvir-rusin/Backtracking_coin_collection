import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Manages the game logic for bots collecting coins in an optimized manner.
 */
public class GameEngine {
    // Array of bot entities in the game
    Bot[] bots;

    // Array of coin entities in the game
    Coin[] coins;

    // Used for generating random positions
    Random random = new Random();

    // Tracks the minimum distance found for the optimal coin collection path
    private double minDistance = Double.MAX_VALUE;

    // Stores the best coin assignment to bots based on the optimization process
    private int[] bestAssignment;

    // Flags to keep track of which coins have been considered in the current assignment calculation
    private boolean[] taken;

    // Dimensions of the game panel
    private int panelWidth, panelHeight;

    // Margins to ensure bots and coins don't spawn too close to the edge
    private int botx=20, boty=20;
    private int coinx=20, coiny=20;

    // Tracks the current score of the game
    private int score = 0;

    // Lists storing the optimal path of coin indices for each bot
    private List<List<Integer>> optimalPaths;

    List<Wall> walls = new ArrayList<>();


    /**
     * Initializes the game engine with the specified number of bots and coins,
     * and sets the dimensions for the game panel.
     */
    public GameEngine(int numberOfBots, int numberOfcoins, int panelWidth, int panelHeight) {
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        bots = new Bot[numberOfBots];
        coins = new Coin[numberOfcoins];
        walls.add(new Wall(100, 100, 200, 100));
        walls.add(new Wall(200, 100, 200, 200));

        // Initialize bots and coins with random positions within the game panel
        for (int i = 0; i < numberOfBots; i++) {
            bots[i] = new Bot(random.nextInt(panelWidth - 3 * botx) + botx, random.nextInt(panelHeight - 3 * boty) + boty);
            System.out.println("Bot position - x: " + bots[i].x + ", y: " + bots[i].y);
        }
        for (int i = 0; i < numberOfcoins; i++) {
            coins[i] = new Coin(random.nextInt(panelWidth - 3 * coinx) + coinx, random.nextInt(panelHeight - 3 * coiny) + coiny);
            System.out.println("Coin position - x: " + coins[i].x + ", y: " + coins[i].y);
        }

        bestAssignment = new int[numberOfBots];
        Arrays.fill(bestAssignment, -1);
        taken = new boolean[numberOfcoins];

        optimalPaths = new ArrayList<>(bots.length);
        for (int i = 0; i < bots.length; i++) {
            optimalPaths.add(new ArrayList<>());
        }
        preComputeRoutesForAllBots();
    }

    private void preComputeRoutesForAllBots() {
        for (int i = 0; i < bots.length; i++) {
            boolean[] visited = new boolean[coins.length];
            List<Integer> currentPath = new ArrayList<>();
            double[] minDistanceWrapper = new double[]{Double.MAX_VALUE};
            List<Integer> optimalPath = new ArrayList<>();
            preComputeRouteForBot(i, visited, currentPath, 0, minDistanceWrapper, optimalPath);
            optimalPaths.set(i, optimalPath); // Assign the computed path to the bot
        }
    }


    private void preComputeRouteForBot(int botIndex, boolean[] visited, List<Integer> currentPath, double currentDistance, double[] minDistanceWrapper, List<Integer> optimalPath) {
        if (currentPath.size() == coins.length) {
            if (currentDistance < minDistanceWrapper[0]) {
                minDistanceWrapper[0] = currentDistance;
                optimalPath.clear();
                optimalPath.addAll(currentPath);
            }
            return;
        }

        for (int i = 0; i < coins.length; i++) {
            if (!visited[i]) {
                visited[i] = true;
                currentPath.add(i);
                double nextDistance = currentDistance;
                if (!currentPath.isEmpty()) {
                    Coin lastCoin = coins[currentPath.get(currentPath.size() - 1)];
                    Coin nextCoin = coins[i];
                    nextDistance += calculateDistance(bots[botIndex].x, bots[botIndex].y, nextCoin.x, nextCoin.y);
                }
                preComputeRouteForBot(botIndex, visited, currentPath, nextDistance, minDistanceWrapper, optimalPath);
                visited[i] = false;
                currentPath.remove(currentPath.size() - 1);
            }
        }
    }

    private double calculateDistance(int x1, int y1, int x2, int y2) {
        // Simple Euclidean distance. Adjust as necessary for your game's mechanics.
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    /**
     * Checks if all coins have been collected.
     *
     * @return true if all coins are collected, false otherwise.
     */
    public boolean allCoinsCollected() {
        for (Coin coin : coins) {
            if (!coin.isCollected()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Updates the positions of all bots towards their assigned coins.
     */
    private boolean canMove(Bot bot, int newX, int newY) {
        for (Wall wall : walls) {
            if (lineIntersectsWall(bot.x, bot.y, newX, newY, wall)) {
                return false; // Movement is blocked by a wall
            }
        }
        return true; // No walls blocking the movement
    }
    private boolean lineIntersectsWall(int x1, int y1, int x2, int y2, Wall wall) {
        // Convert wall endpoints to line segment parameters
        int x3 = wall.startX;
        int y3 = wall.startY;
        int x4 = wall.endX;
        int y4 = wall.endY;

        // Calculate parts of the equations needed for the intersection check
        int den = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (den == 0) {
            return false; // Lines are parallel
        }

        int tNum = (x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4);
        int uNum = -((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3));

        double t = tNum / (double) den;
        double u = uNum / (double) den;

        // If 0<=t<=1 and 0<=u<=1, the line segments intersect
        return t >= 0 && t <= 1 && u >= 0 && u <= 1;
    }

    public void updateBotPositions() {
        for (int i = 0; i < bots.length; i++) {
            if (bestAssignment[i] != -1) {
                Coin targetCoin = coins[bestAssignment[i]];
                // Simplified movement towards the target coin
                if (bots[i].x < targetCoin.x) bots[i].x++;
                else if (bots[i].x > targetCoin.x) bots[i].x--;

                if (bots[i].y < targetCoin.y) bots[i].y++;
                else if (bots[i].y > targetCoin.y) bots[i].y--;
            }
        }

        // Check for reassessment condition
        //int uncollectedCoins = (int) Arrays.stream(coins).filter(coin -> !coin.isCollected()).count();
        //if (uncollectedCoins <= 3) { // Example threshold, adjust based on game dynamics
            //reassessAssignments();
        //}
    }

    /**
     * Optimizes assignments of bots to coins to minimize total distance for the initial collection.
     */
    public void optimizeAssignments() {
        findBestAssignment(new int[bots.length], 0, 0.0);
    }

    /**
     * Recursive function to find the best assignment of coins to bots.
     */
    private void findBestAssignment(int[] currentAssignment, int botIndex, double currentDistance) {
        if (botIndex == bots.length) {//if all the bots got assignment
            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                System.arraycopy(currentAssignment, 0, bestAssignment, 0, bots.length);
            }
            return;
        }

        for (int i = 0; i < coins.length; i++) {
            if (!taken[i]) {
                taken[i] = true;
                double distanceToAdd = DistanceCalculator.calculateDistance(bots[botIndex].x, bots[botIndex].y, coins[i].x, coins[i].y);
                currentAssignment[botIndex] = i; // Assign coin i to bot botIndex

                findBestAssignment(currentAssignment, botIndex + 1, currentDistance + distanceToAdd);
                taken[i] = false; // Backtrack
            }
        }
    }

    /**
     * Returns the assigned coin for a given bot index.
     *
     * @param botIndex Index of the bot.
     * @return Assigned Coin object or null if no valid assignment exists.
     */
    public Coin getAssignedCoin(int botIndex) {
        if (botIndex >= 0 && botIndex < bestAssignment.length) {
            int coinIndex = bestAssignment[botIndex];
            if (coinIndex >= 0 && coinIndex < coins.length) {
                return coins[coinIndex];
            }
        }
        return null;
    }

    /**
     * Updates the game state when bots collect coins, increasing the score accordingly and reassigning to the next closest coin.
     */
    /**
     * Reassigns the bot that just collected a coin to the closest uncollected and unassigned coin.
     * @param botIndex The index of the bot that needs reassignment.
     */
    public void reassignBot(int botIndex) {
        Bot bot = bots[botIndex];
        double minDistance = Double.MAX_VALUE;
        int closestCoinIndex = -1;

        for (int i = 0; i < coins.length; i++) {
            if (!coins[i].isCollected() && !isCoinTargeted(i)) {
                double distance = DistanceCalculator.calculateDistance(bot.x, bot.y, coins[i].x, coins[i].y);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestCoinIndex = i;
                }
            }
        }

        if (closestCoinIndex != -1) {
            bot.currentTarget = closestCoinIndex;
            // Mark the coin as taken or targeted
            bestAssignment[botIndex] = closestCoinIndex;
        }
    }

    /**
     * Checks if a coin is currently targeted by any bot.
     * @param coinIndex The index of the coin to check.
     * @return true if the coin is targeted, false otherwise.
     */
    private boolean isCoinTargeted(int coinIndex) {
        for (Bot bot : bots) {
            if (bot.currentTarget != null && bot.currentTarget == coinIndex) {
                return true;
            }
        }
        return false;
    }
    public void updateCoinCollections() {
        boolean needsReassessment = false;
        for (int i = 0; i < bots.length; i++) {
            Bot bot = bots[i];
            if (bot.currentTarget != null) {
                Coin targetCoin = coins[bot.currentTarget];
                if (!targetCoin.isCollected() && botReachedCoin(bot, targetCoin)) {
                    // Mark the coin as collected
                    targetCoin.collect();
                    score += 10;
                    // Immediately reassign the bot to prevent it from targeting a collected coin
                    reassignBot(i);
                    // Flag that we might need to reassess assignments if other bots were targeting this coin
                    needsReassessment = true;
                }
            } else {
                // If the bot doesn't have a current target, assign one
                reassignBot(i);
            }
        }

        // If any coin was collected, reassess to ensure all bots have correct assignments
        if (needsReassessment) {
            reassessAssignmentsForRemainingBots();
        }
    }

    /**
     * Assigns the closest uncollected coin to each bot after it collects its current target.
     */
    public void assignClosestCoin() {
        // Reset the taken flags for a fresh assignment
        Arrays.fill(taken, false);

        for (int botIndex = 0; botIndex < bots.length; botIndex++) {
            Bot bot = bots[botIndex];
            double minDistance = Double.MAX_VALUE;
            int closestCoinIndex = -1;

            for (int coinIndex = 0; coinIndex < coins.length; coinIndex++) {
                Coin coin = coins[coinIndex];
                if (!coin.isCollected()) {
                    double distance = DistanceCalculator.calculateDistance(bot.x, bot.y, coin.x, coin.y);
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestCoinIndex = coinIndex;
                    }
                }
            }

            // Update the bot's target to the closest uncollected coin
            if (closestCoinIndex != -1) {
                bestAssignment[botIndex] = closestCoinIndex;
                // Optionally mark the coin as "taken" if you want to prevent other bots from targeting it
                taken[closestCoinIndex] = true;
            }
        }
    }

    /**
     * Reassesses and reassigns bots to the closest uncollected and unassigned coins.
     * Intended to be used when the number of remaining uncollected coins is low.
     */
    public void reassessAssignments() {
        // First, mark all coins as unassigned
        Arrays.fill(taken, false);
        for (Bot bot : bots) {
            bot.currentTarget = null; // Reset current target for all bots
        }

        // Then, reassign each bot to its closest coin
        for (int i = 0; i < bots.length; i++) {
            reassignBot(i);
        }
    }

    public void reassessAssignmentsForRemainingBots() {
        // Iterate through all bots
        for (int i = 0; i < bots.length; i++) {
            Bot bot = bots[i];
            // Check if the bot's target has been collected or if it's targeting a collected coin
            if (bot.currentTarget == null || coins[bot.currentTarget].isCollected()) {
                reassignBot(i); // Reassign bot to a new, uncollected coin
            }
        }
    }

    /**
     * Checks if a bot has reached its assigned coin.
     *
     * @param bot The bot in question.
     * @param coin The target coin.
     * @return true if the bot is close enough to collect the coin, false otherwise.
     */
    private boolean botReachedCoin(Bot bot, Coin coin) {
        final double distanceThreshold = 5.0; // Define proximity for coin collection
        return DistanceCalculator.calculateDistance(bot.x, bot.y, coin.x, coin.y) <= distanceThreshold;
    }
}
