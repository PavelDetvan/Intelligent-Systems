import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestAgentPerformance2 {

    // Enhanced QTable dimensions: 10 x 2 x 10 x 2.
    static double[][][][] QTable = new double[10][2][10][2];

    // Number of games to simulate (adjust as needed)
    static final long NUM_GAMES = 1000000; // e.g., 10 million games

    public static void main(String[] args) {
        // Load the enhanced QTable from file (e.g., "qtable2.csv")
        QTable = loadQTable("qtable2.csv");

        // Create the environment without rendering.
        BlackJackEnv game = new BlackJackEnv(BlackJackEnv.NONE);

        // Outcome counters and record of player total when standing.
        long winCount = 0, lossCount = 0, tieCount = 0;
        long totalStandTotal = 0;
        long standCount = 0;

        // Simulate games.
        for (long i = 0; i < NUM_GAMES; i++) {
            ArrayList<String> state = game.reset();
            int[] indices = getStateIndices(state);

            // indices[0]: player total index, indices[1]: active Ace index, indices[2]: dealer card index.
            while (isGameNotOver(state)) {
                int action;
                // Use a greedy policy for testing.
                action = argMax(QTable[indices[0]][indices[1]][indices[2]]);
                if (action == 1) {
                    standCount++;
                    // Record player's total when standing.
                    int playerTotal = BlackJackEnv.totalValue(BlackJackEnv.getPlayerCards(state));
                    totalStandTotal += playerTotal;
                }
                state = game.step(action);
                indices = getStateIndices(state);
            }
            int reward = Integer.parseInt(state.get(1));
            if (reward == 1)
                winCount++;
            else if (reward == -1)
                lossCount++;
            else
                tieCount++;
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

    // Loads the enhanced QTable from CSV.
    // Expected CSV format: PlayerTotal,ActiveAce,DealerCard,Hit,Stand
    public static double[][][][] loadQTable(String filename) {
        double[][][][] table = new double[10][2][10][2];
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                int playerTotal = Integer.parseInt(parts[0]);  // should be between 12 and 21
                int activeAce = Integer.parseInt(parts[1]);      // 0 or 1
                int dealerCard = Integer.parseInt(parts[2]);       // 2-10 or 11
                double hit = Double.parseDouble(parts[3]);
                double stand = Double.parseDouble(parts[4]);
                int pIndex = playerTotal - 12;
                int aceIndex = activeAce;
                int dealerIndex = (dealerCard == 11) ? 9 : dealerCard - 2;
                table[pIndex][aceIndex][dealerIndex][0] = hit;
                table[pIndex][aceIndex][dealerIndex][1] = stand;
            }
            System.out.println("Enhanced QTable loaded from " + filename);
        } catch (IOException e) {
            System.err.println("Error loading enhanced QTable: " + e.getMessage());
        }
        return table;
    }

    // Returns composite state indices: {playerIndex, activeAceIndex, dealerIndex}.
    private static int[] getStateIndices(ArrayList<String> state) {
        List<String> playerCards = BlackJackEnv.getPlayerCards(state);
        int playerTotal = BlackJackEnv.totalValue(playerCards);
        int playerIndex = playerTotal - 12;  // valid only if total in [12,21]
        boolean hasActiveAce = BlackJackEnv.holdActiveAce(playerCards);
        int aceIndex = hasActiveAce ? 1 : 0;
        List<String> dealerCards = BlackJackEnv.getDealerCards(state);
        int dealerVal = BlackJackEnv.valueOf(dealerCards.get(0)); // dealer's first card
        int dealerIndex = (dealerVal == 11) ? 9 : dealerVal - 2;
        return new int[]{playerIndex, aceIndex, dealerIndex};
    }

    private static boolean isGameNotOver(ArrayList<String> state) {
        return state.get(0).equals("false");
    }

    private static int argMax(double[] qValues) {
        return (qValues[1] > qValues[0]) ? 1 : 0;
    }
}
