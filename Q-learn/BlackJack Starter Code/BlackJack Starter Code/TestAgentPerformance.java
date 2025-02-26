import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestAgentPerformance {

    // We use a 10x2 QTable for the simple agent (states corresponding to player totals 12 to 21).
    static double[][] QTable = new double[10][2];

    // Number of games to simulate (adjust as needed, e.g., 10 million for large-scale testing).
    static final long NUM_GAMES = 1000000; // For testing, 100000 games

    public static void main(String[] args) {
        // Load the trained QTable from file ("qtable1.csv").
        QTable = loadQTable("qtable1.csv");

        // Create the environment without rendering for speed.
        BlackJackEnv game = new BlackJackEnv(BlackJackEnv.NONE);

        // Counters for outcomes and to record the player's total when the agent chooses to stand.
        long winCount = 0;
        long lossCount = 0;
        long tieCount = 0;
        long totalStandTotal = 0;
        long standCount = 0;

        // Run simulation.
        for (long i = 0; i < NUM_GAMES; i++) {
            ArrayList<String> state = game.reset();
            int playerTotal = extractPlayerTotal(state);
            int stateIndex = playerTotal - 12;  // Map totals 12-21 to indices 0-9

            // Simulate one game.
            while (isGameNotOver(state)) {
                int action;
                // Use a greedy policy for testing (no exploration).
                action = argMax(QTable[stateIndex]);
                
                // If the agent chooses STAND, record the player's total.
                if (action == 1) {
                    standCount++;
                    totalStandTotal += playerTotal;
                }
                
                state = game.step(action);
                playerTotal = extractPlayerTotal(state);
                stateIndex = playerTotal - 12;
            }

            // Determine outcome from the terminal state.
            int reward = Integer.parseInt(state.get(1));
            if (reward == 1) {
                winCount++;
            } else if (reward == -1) {
                lossCount++;
            } else {
                tieCount++;
            }
        }
        
        long totalGames = winCount + lossCount + tieCount;
        System.out.println("Total games: " + totalGames);
        System.out.printf("Wins: %d (%.2f%%)%n", winCount, 100.0 * winCount / totalGames);
        System.out.printf("Losses: %d (%.2f%%)%n", lossCount, 100.0 * lossCount / totalGames);
        System.out.printf("Ties: %d (%.2f%%)%n", tieCount, 100.0 * tieCount / totalGames);
        if (standCount > 0) {
            System.out.printf("Average player total when standing: %.2f%n", totalStandTotal / (double)standCount);
        } else {
            System.out.println("No STAND actions recorded.");
        }
    }

    // Helper method: Load the simple QTable (10x2) from CSV.
    // Expected CSV format: State,Hit,Stand (with State values 12 through 21).
    public static double[][] loadQTable(String filename) {
        double[][] table = new double[10][2];
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                int index = Integer.parseInt(parts[0]);
                // Only process indices 0 through 9
                if (index < 0 || index > 9) continue;
                table[index][0] = Double.parseDouble(parts[1]);
                table[index][1] = Double.parseDouble(parts[2]);
            }
            System.out.println("QTable loaded from " + filename);
        } catch (IOException e) {
            System.err.println("Error loading QTable: " + e.getMessage());
        }
        return table;
    }

    // Helper method: Extract the player's total from the game state.
    private static int extractPlayerTotal(ArrayList<String> state) {
        List<String> playerCards = BlackJackEnv.getPlayerCards(state);
        return BlackJackEnv.totalValue(playerCards);
    }

    // Helper method: Check if the game is still ongoing.
    private static boolean isGameNotOver(ArrayList<String> state) {
        return state.get(0).equals("false");
    }

    // Helper method: argMax returns the index of the higher Q-value (0 = HIT, 1 = STAND).
    private static int argMax(double[] qValues) {
        return (qValues[1] > qValues[0]) ? 1 : 0;
    }
}
