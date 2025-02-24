import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class PlayAgainstAgent {
    // Use the enhanced QTable dimensions (if you trained with simple state, adjust accordingly)
    // For a simple QTable, use: static double[][] QTable = new double[32][2];
    // For the enhanced version (if you trained with composite state), use the corresponding dimensions.
    // Here we assume a simple 32x2 QTable for demonstration; adjust if using enhanced.
    static double[][] QTable = new double[32][2];
    
    // Delays for GUI updates (set as desired)
    static final int STEP_DELAY = 1000; // Delay between moves (milliseconds)
    static final int GAME_DELAY = 2000; // Delay between games

    public static void main(String[] args) {
        // Load the trained QTable from file.
        QTable = loadQTable("qtable.csv");
        
        // Create a HumanDealerBlackJackEnv (the environment where YOU act as dealer).
        HumanDealerBlackJackEnv game = new HumanDealerBlackJackEnv(HumanDealerBlackJackEnv.RENDER);
        
        // Counters for game outcomes.
        int winCount = 0;
        int lossCount = 0;
        int tieCount = 0;
        
        // Let's play a desired number of games (e.g., 5 or more).
        int numberOfGamesToPlay = 5;
        Scanner sc = new Scanner(System.in); // if additional dealer input is needed

        for (int gameIndex = 0; gameIndex < numberOfGamesToPlay; gameIndex++) {
            ArrayList<String> gamestate = game.reset();
            System.out.println("\nGame " + (gameIndex + 1) + " initial state: " + gamestate);
            
            // Play one game until it ends.
            while (gamestate.get(0).equals("false")) { // game is not over
                System.out.println("Dealer's cards: " + HumanDealerBlackJackEnv.getDealerCards(gamestate));
                System.out.println("Player's cards: " + HumanDealerBlackJackEnv.getPlayerCards(gamestate));
                
                // For simple state representation, extract player's total:
                int playerTotal = extractPlayerTotal(gamestate);
                // Use the QTable to select the best action (0 = HIT, 1 = STAND)
                int action = argMax(QTable[playerTotal]);
                
                if (action == 0)
                    System.out.println("Agent decides to HIT.");
                else
                    System.out.println("Agent decides to STAND.");
                
                // Execute the chosen action.
                gamestate = game.step(action);
                System.out.println("New game state: " + gamestate);
                System.out.println("Reward received: " + gamestate.get(1));
                
                // Pause between moves.
                try {
                    Thread.sleep(STEP_DELAY);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            // Determine outcome.
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
            System.out.println("Dealer's final cards: " + HumanDealerBlackJackEnv.getDealerCards(gamestate) +
                    " (total = " + HumanDealerBlackJackEnv.totalValue(HumanDealerBlackJackEnv.getDealerCards(gamestate)) + ")");
            System.out.println("Player's final cards: " + HumanDealerBlackJackEnv.getPlayerCards(gamestate) +
                    " (total = " + HumanDealerBlackJackEnv.totalValue(HumanDealerBlackJackEnv.getPlayerCards(gamestate)) + ")");
            
            // Pause between games.
            try {
                Thread.sleep(GAME_DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Summary of results.
        int totalGames = winCount + lossCount + tieCount;
        System.out.println("\nSummary of " + numberOfGamesToPlay + " games:");
        System.out.printf("Wins: %d (%.2f%%)%n", winCount, 100.0 * winCount / totalGames);
        System.out.printf("Losses: %d (%.2f%%)%n", lossCount, 100.0 * lossCount / totalGames);
        System.out.printf("Ties: %d (%.2f%%)%n", tieCount, 100.0 * tieCount / totalGames);
    }

    // Helper method to load the simple QTable from CSV.
    private static double[][] loadQTable(String filename) {
        double[][] table = new double[32][2];
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // skip header
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

    // Extract player's total from state using BlackJackEnv helper.
    private static int extractPlayerTotal(ArrayList<String> state) {
        List<String> playerCards = HumanDealerBlackJackEnv.getPlayerCards(state);
        return HumanDealerBlackJackEnv.totalValue(playerCards);
    }

    // Returns index of the action with highest Q-value.
    private static int argMax(double[] qValues) {
        return (qValues[1] > qValues[0]) ? 1 : 0;
    }
}
