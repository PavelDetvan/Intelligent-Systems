import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class QLearner2 {
    
    // Learning parameters
    static final double ALPHA = 0.1;    // Learning rate
    static final double GAMMA = 0.95;   // Discount factor
    static final double EPSILON = 0.1;  // Exploration rate for training


    // QTable dimensions: 
    // - Player total: 12-21 -> 10 values (index = total - 12)
    // - Active Ace: 2 values (0 = no, 1 = yes)
    // - Dealer card: values 2-10 and Ace (11) -> 10 values (index: if value==11 then 9 else value-2)
    // - Actions: 2 (0 = HIT, 1 = STAND)
    static double[][][][] QTable = new double[10][2][10][2];

    // Main method
    public static void main(String[] args) {
        // Use environment without rendering
        BlackJackEnv game = new BlackJackEnv(BlackJackEnv.NONE);

        // Variables to measure performance
        double totalReward = 0.0;
        int numberOfGames = 0;
        ArrayList<Double> avgRewards = new ArrayList<>();

        // Training loop: run for a fixed number of episodes
        while (notDone()) {
            totalReward += playOneGame(game, QTable);
            numberOfGames++;
            if ((numberOfGames % 10000) == 0) {
                double avg = totalReward / numberOfGames;
                System.out.println("Avg reward after " + numberOfGames + " games = " + avg);
                avgRewards.add(avg);
            }
        }
        // Show learned Q-table
        outputQTable(QTable);

        // Save trained Q-table and performance metrics
        saveQTable(QTable, "qtable2.csv");
        savePerformanceMetrics(avgRewards, "performance_metrics2.csv");
    }

    // playOneGame: Plays one episode and updates the QTable
    private static double playOneGame(BlackJackEnv game, double[][][][] QTable) {
        // Reset game and extract initial state
        ArrayList<String> state = game.reset();
        int[] indices = getStateIndices(state);
        // indices[0]: player total index, indices[1]: active Ace index, indices[2]: dealer card index

        // Loop until game is over
        while (isGameNotOver(state)) {
            int action;
            // Epsilon-greedy action selection: With probability EPSILON, pick a random action
            if (Math.random() < EPSILON) {
                action = (Math.random() < 0.5) ? 0 : 1; // Randomly choose HIT or STAND
            } else {
                // Otherwise, choose the action with the highest Q-value for the current state
                action = argMax(QTable[indices[0]][indices[1]][indices[2]]);
            }

            // Execute action
            ArrayList<String> nextState = game.step(action);
            int reward = Integer.parseInt(nextState.get(1));
            int[] nextIndices = getStateIndices(nextState);

            // Get max Q for next state (if game not over)
            double maxNextQ = isGameNotOver(nextState) ?
                    Math.max(QTable[nextIndices[0]][nextIndices[1]][nextIndices[2]][0],
                             QTable[nextIndices[0]][nextIndices[1]][nextIndices[2]][1])
                    : 0.0;

            // Q-learning update
            QTable[indices[0]][indices[1]][indices[2]][action] =
                    QTable[indices[0]][indices[1]][indices[2]][action]
                    + ALPHA * (reward + GAMMA * maxNextQ - QTable[indices[0]][indices[1]][indices[2]][action]);

            // Move to next state
            state = nextState;
            indices = nextIndices;
        }
        // Return final reward
        return Integer.parseInt(state.get(1));
    }


    // getStateIndices: Returns indices for the composite state.
    private static int[] getStateIndices(ArrayList<String> state) {
        // Extract player's total and active Ace
        List<String> playerCards = BlackJackEnv.getPlayerCards(state);
        int playerTotal = BlackJackEnv.totalValue(playerCards);
        // Only consider decision states from 12 to 21
        int playerIndex = playerTotal - 12;
        // Get active Ace indicator
        boolean hasActiveAce = BlackJackEnv.holdActiveAce(playerCards);
        int aceIndex = hasActiveAce ? 1 : 0;

        // Extract dealer's visible card
        List<String> dealerCards = BlackJackEnv.getDealerCards(state);
        int dealerVal = BlackJackEnv.valueOf(dealerCards.get(0)); // dealer's first card
        int dealerIndex = (dealerVal == 11) ? 9 : dealerVal - 2;  // map 2->0,3->1,...,10->8,11->9

        return new int[]{playerIndex, aceIndex, dealerIndex};
    }

    // Helper method: isGameNotOver
    // Checks if the game is still ongoing.
    private static boolean isGameNotOver(ArrayList<String> state) {
        return state.get(0).equals("false");
    }

    // Helper method: argMax
    // Returns the index (action) with the highest Q-value.
    private static int argMax(double[] qValues) {
        return (qValues[1] > qValues[0]) ? 1 : 0;
    }

    // Stopping condition for training
    private static int episodeCounter = 0;
    private static boolean notDone() {
        episodeCounter++;
        return (episodeCounter <= 1000000);
    }

    // outputQTable: Prints the learned Q-table.
    private static void outputQTable(double[][][][] QTable) {
        System.out.println("\nLearned Q-Table (player total index [0-9], activeAce [0,1], dealer card index [0-9]):");
        for (int p = 0; p < QTable.length; p++) {
            int playerTotal = p + 12; // convert index back to total
            for (int a = 0; a < 2; a++) {
                for (int d = 0; d < QTable[p][a].length; d++) {
                    int dealerCard = (d == 9) ? 11 : d + 2;
                    System.out.printf("PlayerTotal = %2d, ActiveAce = %d, DealerCard = %2d: HIT = %.3f, STAND = %.3f%n",
                            playerTotal, a, dealerCard, QTable[p][a][d][0], QTable[p][a][d][1]);
                }
            }
        }
    }

    // saveQTable: Saves the Q-table to CSV file.
    public static void saveQTable(double[][][][] QTable, String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            // Write header
            pw.println("PlayerTotal,ActiveAce,DealerCard,Hit,Stand");
            for (int p = 0; p < QTable.length; p++) {
                int playerTotal = p + 12;
                for (int a = 0; a < 2; a++) {
                    for (int d = 0; d < QTable[p][a].length; d++) {
                        int dealerCard = (d == 9) ? 11 : d + 2;
                        pw.printf("%d,%d,%d,%.5f,%.5f%n", playerTotal, a, dealerCard,
                                QTable[p][a][d][0], QTable[p][a][d][1]);
                    }
                }
            }
            System.out.println("QTable saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving QTable: " + e.getMessage());
        }
    }

    // savePerformanceMetrics: Saves performance metrics to CSV file.
    public static void savePerformanceMetrics(ArrayList<Double> avgRewards, String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
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
