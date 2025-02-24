import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TrainedAgentGUI2 {
    // Enhanced QTable dimensions:
    // Player totals 12-21: 10 values (index = total - 12)
    // Active Ace: 2 values (0 = no, 1 = yes)
    // Dealer card: 10 values (2-10 and Ace (11); index = (dealer==11? 9 : dealer - 2))
    // Actions: 2 (0 = HIT, 1 = STAND)
    static double[][][][] QTable = new double[10][2][10][2];
    
    // --- Delays (in milliseconds) ---
    static final int STEP_DELAY = 3000; // Delay between moves (adjust as desired)
    static final int GAME_DELAY = 10000; // Delay between games (adjust as desired)

    public static void main(String[] args) {
        // Load the trained enhanced QTable from file.
        QTable = loadQTable("qtable.csv");
        
        // Create a BlackJack environment with rendering enabled.
        BlackJackEnv game = new BlackJackEnv(BlackJackEnv.RENDER);
        
        // Counters for win, loss, and tie.
        int winCount = 0;
        int lossCount = 0;
        int tieCount = 0;
        
        // Let's play 50 games using our trained agent.
        for (int gameIndex = 0; gameIndex < 5; gameIndex++) {
            ArrayList<String> gamestate = game.reset();
            System.out.println("\nGame " + (gameIndex + 1) + " initial state: " + gamestate);
            
            // Play one game until it ends.
            while (gamestate.get(0).equals("false")) { // while game is not over
                System.out.println("Dealer's cards: " + BlackJackEnv.getDealerCards(gamestate));
                System.out.println("Player's cards: " + BlackJackEnv.getPlayerCards(gamestate));
                
                // Extract the composite state indices.
                int[] stateIndices = getStateIndices(gamestate);
                // Use the QTable to select the best action.
                int action = argMax(QTable[stateIndices[0]][stateIndices[1]][stateIndices[2]]);
                
                if (action == 0)
                    System.out.println("Agent decides to HIT.");
                else
                    System.out.println("Agent decides to STAND.");
                
                // Execute the chosen action.
                gamestate = game.step(action);
                System.out.println("New game state: " + gamestate);
                System.out.println("Reward received: " + gamestate.get(1));
                
                // Pause so you can observe the changes.
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
            
            // Pause between games.
            try {
                Thread.sleep(GAME_DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Print summary of results.
        int totalGames = winCount + lossCount + tieCount;
        System.out.println("\nSummary of 50 games:");
        System.out.printf("Wins: %d (%.2f%%)%n", winCount, 100.0 * winCount / totalGames);
        System.out.printf("Losses: %d (%.2f%%)%n", lossCount, 100.0 * lossCount / totalGames);
        System.out.printf("Ties: %d (%.2f%%)%n", tieCount, 100.0 * tieCount / totalGames);
    }

    /**
     * Loads the enhanced QTable from a CSV file.
     * Expected CSV format: PlayerTotal,ActiveAce,DealerCard,Hit,Stand
     */
    private static double[][][][] loadQTable(String filename) {
        double[][][][] table = new double[10][2][10][2];
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                int playerTotal = Integer.parseInt(parts[0]);  // Expected range: 12-21
                int activeAce = Integer.parseInt(parts[1]);      // 0 or 1
                int dealerCard = Integer.parseInt(parts[2]);       // Expected: 2-10 or 11 (for Ace)
                double hit = Double.parseDouble(parts[3]);
                double stand = Double.parseDouble(parts[4]);
                
                int pIndex = playerTotal - 12;
                int aceIndex = activeAce; 
                int dealerIndex = (dealerCard == 11) ? 9 : dealerCard - 2;
                
                table[pIndex][aceIndex][dealerIndex][0] = hit;
                table[pIndex][aceIndex][dealerIndex][1] = stand;
            }
            System.out.println("QTable loaded from " + filename);
        } catch (IOException e) {
            System.err.println("Error loading QTable: " + e.getMessage());
        }
        return table;
    }

    /**
     * Extracts composite state indices from the game state.
     * Returns an array: {playerIndex, activeAceIndex, dealerIndex}.
     */
    private static int[] getStateIndices(ArrayList<String> state) {
        // Get player's cards and compute total.
        List<String> playerCards = BlackJackEnv.getPlayerCards(state);
        int playerTotal = BlackJackEnv.totalValue(playerCards);
        int playerIndex = playerTotal - 12; // Only valid when total is between 12 and 21.
        
        // Determine if player has an active Ace.
        boolean hasActiveAce = BlackJackEnv.holdActiveAce(playerCards);
        int aceIndex = hasActiveAce ? 1 : 0;
        
        // Get dealer's first card value.
        List<String> dealerCards = BlackJackEnv.getDealerCards(state);
        int dealerVal = BlackJackEnv.valueOf(dealerCards.get(0));
        int dealerIndex = (dealerVal == 11) ? 9 : dealerVal - 2;
        
        return new int[] {playerIndex, aceIndex, dealerIndex};
    }

    /**
     * Returns the index (action) corresponding to the highest Q-value.
     * Index 0 corresponds to HIT, index 1 to STAND.
     */
    private static int argMax(double[] qValues) {
        return (qValues[1] > qValues[0]) ? 1 : 0;
    }
}
