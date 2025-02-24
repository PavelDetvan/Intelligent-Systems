import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TrainedAgentGUI {
    // QTable dimensions: 32 rows (player totals 0 to 31) x 2 columns (0 = HIT, 1 = STAND)
    static double[][] QTable = new double[32][2];
    
    // --- Delays (in milliseconds) ---
    static final int STEP_DELAY = 1; // Delay between moves
    static final int GAME_DELAY = 2; // Delay between games

    public static void main(String[] args) {
        // Load the trained QTable from file.
        QTable = loadQTable("qtable.csv");
        
        // Create a BlackJack environment with rendering enabled.
        BlackJackEnv game = new BlackJackEnv(BlackJackEnv.RENDER);
        
        // Counters for win, loss, and tie.
        int winCount = 0;
        int lossCount = 0;
        int tieCount = 0;
        
        // Let's play 5 games using our trained agent.
        for (int gameIndex = 0; gameIndex < 50; gameIndex++) {
            ArrayList<String> gamestate = game.reset();
            System.out.println("\nGame " + (gameIndex + 1) + " initial state: " + gamestate);
            
            // Play one game until it ends.
            while (gamestate.get(0).equals("false")) { // while game is not over
                System.out.println("Dealer's cards: " + BlackJackEnv.getDealerCards(gamestate));
                System.out.println("Player's cards: " + BlackJackEnv.getPlayerCards(gamestate));
                
                // Extract player's total from the game state.
                int playerTotal = extractPlayerTotal(gamestate);
                // Use the QTable to select the best action (0 = HIT, 1 = STAND).
                int action = argMax(QTable[playerTotal]);
                
                if (action == 0)
                    System.out.println("Agent decides to HIT.");
                else
                    System.out.println("Agent decides to STAND.");
                
                // Take the chosen action.
                gamestate = game.step(action);
                System.out.println("New game state: " + gamestate);
                System.out.println("Reward received: " + gamestate.get(1));
                
                // Pause for a moment so you can observe the changes.
                try {
                    Thread.sleep(STEP_DELAY);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            // Game has ended, determine outcome.
            int reward = Integer.parseInt(gamestate.get(1));
            String result;
            if (reward == 1) {
                result = "WIN";
                winCount++;
            } else if (reward == -1) {
                result = "LOSS";
                lossCount++;
            } else {
                result = "TIE";
                tieCount++;
            }
            System.out.println("Game " + (gameIndex + 1) + " ended with a " + result + ".");
            System.out.println("Dealer's final cards: " + BlackJackEnv.getDealerCards(gamestate) +
                    " (total = " + BlackJackEnv.totalValue(BlackJackEnv.getDealerCards(gamestate)) + ")");
            System.out.println("Player's final cards: " + BlackJackEnv.getPlayerCards(gamestate) +
                    " (total = " + BlackJackEnv.totalValue(BlackJackEnv.getPlayerCards(gamestate)) + ")");
            
            // Pause between games so you can see the results.
            try {
                Thread.sleep(GAME_DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Print summary of results.
        int totalGames = winCount + lossCount + tieCount;
        System.out.println("\nSummary of 5 games:");
        System.out.printf("Wins: %d (%.2f%%)%n", winCount, 100.0 * winCount / totalGames);
        System.out.printf("Losses: %d (%.2f%%)%n", lossCount, 100.0 * lossCount / totalGames);
        System.out.printf("Ties: %d (%.2f%%)%n", tieCount, 100.0 * tieCount / totalGames);
    }

    /**
     * Loads the QTable from a CSV file.
     * Expected CSV format: State,Hit,Stand (one row per state).
     */
    private static double[][] loadQTable(String filename) {
        double[][] table = new double[32][2];
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                int stateIndex = Integer.parseInt(parts[0]);
                table[stateIndex][0] = Double.parseDouble(parts[1]);
                table[stateIndex][1] = Double.parseDouble(parts[2]);
            }
            System.out.println("QTable loaded from " + filename);
        } catch (IOException e) {
            System.err.println("Error loading QTable: " + e.getMessage());
        }
        return table;
    }

    /**
     * Extracts the player's total card value from the gamestate.
     * Uses the helper methods from BlackJackEnv.
     */
    private static int extractPlayerTotal(ArrayList<String> state) {
        List<String> playerCards = BlackJackEnv.getPlayerCards(state);
        return BlackJackEnv.totalValue(playerCards);
    }

    /**
     * Returns the index (action) corresponding to the highest Q-value for a given state.
     * In our case, index 0 corresponds to HIT and index 1 corresponds to STAND.
     */
    private static int argMax(double[] qValues) {
        return (qValues[1] > qValues[0]) ? 1 : 0;
    }
}
