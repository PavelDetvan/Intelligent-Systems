import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class QLearner1 {

    // Learning parameters
    static final double ALPHA = 0.1;    // Learning rate
    static final double GAMMA = 0.95;   // Discount factor
    static final double EPSILON = 0.1;  // Exploration rate for training

    // QTable
    // Using only player total as state, and actions HIT and STAND.
    // Decisions are only needed for totals 12-21, so we only need 10 values.
    static double[][] QTable = new double[10][2];  // Two actions: index 0 = HIT, index 1 = STAND

    // Main method
    public static void main(String[] args) {
        // Use environment without rendering
        BlackJackEnv game = new BlackJackEnv(BlackJackEnv.NONE);

        // Variables to measure performance
        double totalReward = 0.0;
        int numberOfGames = 0;
        
        // Log performance metrics
        ArrayList<Double> avgRewards = new ArrayList<>();

        // Training loop: run for a fixed number of episodes
        while (notDone()) {
            // playOneGame returns the final reward from that game
            totalReward += playOneGame(game, QTable);
            numberOfGames++;
            // Print average reward every 10,000 episodes
            if ((numberOfGames % 10000) == 0) {
                double avg = totalReward / numberOfGames;
                System.out.println("Avg reward after " + numberOfGames + " games = " + avg);
                avgRewards.add(avg);
            }
        }
        // Show learned Q-table
        outputQTable(QTable);
        
        // Save trained Q-table and performance metrics
        saveQTable(QTable, "qtable1.csv");
        savePerformanceMetrics(avgRewards, "performance_metrics1.csv");
    }

    // playOneGame: Plays one episode and updates the QTable
    private static double playOneGame(BlackJackEnv game, double[][] QTable) {
        // Reset game and extract initial state
        ArrayList<String> state = game.reset();
        int playerTotal = extractPlayerTotal(state);
        int stateIndex = playerTotal - 12;  // Index in QTable 
        // Loop until game is over
        while (isGameNotOver(state)) {
            int action;
            // Epsilon-greedy action selection: With probability EPSILON, pick a random action
            if (Math.random() < EPSILON) {
                action = (Math.random() < 0.5) ? 0 : 1;  // Randomly choose HIT or STAND
            } else {
                // Otherwise, choose the action with the highest Q-value for the current state
                action = argMax(QTable[stateIndex]);
            }

            // Execute action
            ArrayList<String> nextState = game.step(action);
            // Immediate reward stored as the second element in the state
            int reward = Integer.parseInt(nextState.get(1));
            int nextPlayerTotal = extractPlayerTotal(nextState);
            int nextStateIndex = nextPlayerTotal - 12;  // Index in QTable

            // Compute maximum Q-value for the next state, if game is not over
            double maxNextQ = isGameNotOver(nextState) ? 
                Math.max(QTable[nextStateIndex][0], QTable[nextStateIndex][1]) : 0.0;

            // Q-Learning update:
            // Q(s,a) <- Q(s,a) + alpha * (r + gamma * maxQ(s',a') - Q(s,a))
            QTable[stateIndex][action] = QTable[stateIndex][action] 
                    + ALPHA * (reward + GAMMA * maxNextQ - QTable[stateIndex][action]);

            // Move to the next state
            state = nextState;
            playerTotal = extractPlayerTotal(state);
            stateIndex = playerTotal - 12;
        }
        // Return the final reward (second element of the end state)
        return Integer.parseInt(state.get(1));
    }

    // Helper method: extractPlayerTotal
    // Extracts the player's total card value from gamestate.
    private static int extractPlayerTotal(ArrayList<String> state) {
        List<String> playerCards = BlackJackEnv.getPlayerCards(state);
        return BlackJackEnv.totalValue(playerCards);
    }

    // Helper method: isGameNotOver
    // Checks if the game is still ongoing.
    private static boolean isGameNotOver(ArrayList<String> state) {
        // state.get(0) equals "false" when the game is still going
        return state.get(0).equals("false");
    }


    // Helper method: argMax
    // Returns the index (action) with the highest Q-value.
    private static int argMax(double[] qValues) {
        // If both actions have the same value, default to HIT (0)
        return (qValues[1] > qValues[0]) ? 1 : 0;
    }

    // Stopping condition for training
    private static int episodeCounter = 0;
    private static boolean notDone() {
        episodeCounter++;
        return (episodeCounter <= 1000000);
    }

    // outputQTable: Prints the learned Q-table.
    private static void outputQTable(double[][] QTable) {
        System.out.println("\nLearned Q-Table (player total index [0-31]):");
        // Print states from total = 12 upward (decision-relevant states).
        for (int total = 12; total < QTable.length; total++) {
            System.out.printf("Total = %2d: HIT = %.3f, STAND = %.3f%n", 
                              total, QTable[total][0], QTable[total][1]);
        }
    }

    // saveQTable: Saves the Q-table to CSV file.
    public static void saveQTable(double[][] QTable, String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            // Write header
            pw.println("State,Hit,Stand");
            for (int total = 0; total < QTable.length; total++) {
                pw.printf("%d,%.5f,%.5f%n", total, QTable[total][0], QTable[total][1]);
            }
            System.out.println("QTable saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving QTable: " + e.getMessage());
        }
    }

    // savePerformanceMetrics: Saves performance metrics to CSV file.
    public static void savePerformanceMetrics(ArrayList<Double> avgRewards, String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            // Write header
            pw.println("Episode,AverageReward");
            int episodeStep = 10000;
            for (int i = 0; i < avgRewards.size(); i++) {
                pw.printf("%d,%.5f%n", (i+1)*episodeStep, avgRewards.get(i));
            }
            System.out.println("Performance metrics saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving performance metrics: " + e.getMessage());
        }
    }
}
