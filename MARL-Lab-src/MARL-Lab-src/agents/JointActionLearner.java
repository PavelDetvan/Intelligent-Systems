package agents;

public class JointActionLearner implements Agent {

    private double[][] Q;       // Q-values for each (own action, opponent action) pair
    private int[] oppCount;     // Count of opponent's actions for each action index
    private int oppTotal;       // Total count of opponent actions observed
    private double alpha;       // Learning rate
    private double tau;         // Temperature parameter for Boltzmann exploration
    private int numberOfActions;

    // Constructor: Initialize Q-table, opponent counts, and parameters
    public JointActionLearner(int numberOfActions) {
        this.numberOfActions = numberOfActions;
        Q = new double[numberOfActions][numberOfActions];
        oppCount = new int[numberOfActions];
        oppTotal = 0;
        // Initialize Q-values to small random values
        for (int a = 0; a < numberOfActions; a++) {
            for (int b = 0; b < numberOfActions; b++) {
                Q[a][b] = -0.1 + Math.random() * 0.2;
            }
        }
        alpha = 0.01;   // You can experiment with this value
        tau = 0.2;      // Initial temperature for exploration
    }

    // Helper: Estimate opponent's action probabilities based on past observations.
    private double[] getOpponentProbabilities() {
        double[] p = new double[numberOfActions];
        if (oppTotal == 0) {
            // No data yet: assume uniform distribution
            for (int i = 0; i < numberOfActions; i++) {
                p[i] = 1.0 / numberOfActions;
            }
        } else {
            for (int i = 0; i < numberOfActions; i++) {
                p[i] = ((double) oppCount[i]) / oppTotal;
            }
        }
        return p;
    }

    // Helper: Compute the expected Q-value for each of the agent's actions.
    // For each own action, E[Q(a)] = sum_over_opponent( p(opponent)*Q[a][opponent] ).
    private double[] getExpectedQ() {
        double[] expected = new double[numberOfActions];
        double[] oppProb = getOpponentProbabilities();
        for (int a = 0; a < numberOfActions; a++) {
            double sum = 0;
            for (int b = 0; b < numberOfActions; b++) {
                sum += oppProb[b] * Q[a][b];
            }
            expected[a] = sum;
        }
        return expected;
    }

    // Helper: Compute a softmax probability distribution from expected Q-values.
    private double[] softmax(double[] expected) {
        double sumExp = 0.0;
        double[] expValues = new double[numberOfActions];
        for (int i = 0; i < numberOfActions; i++) {
            expValues[i] = Math.exp(expected[i] / tau);
            sumExp += expValues[i];
        }
        double[] probs = new double[numberOfActions];
        for (int i = 0; i < numberOfActions; i++) {
            probs[i] = expValues[i] / sumExp;
        }
        return probs;
    }

    // Returns the probability of selecting action i (for visualization).
    @Override
    public double actionProb(int i) {
        double[] expected = getExpectedQ();
        double[] probs = softmax(expected);
        return probs[i];
    }

    // Select an action according to the Boltzmann (softmax) probability distribution.
    @Override
    public int selectAction() {
        double[] expected = getExpectedQ();
        double[] probs = softmax(expected);
        double r = Math.random();
        double cumulative = 0.0;
        for (int i = 0; i < numberOfActions; i++) {
            cumulative += probs[i];
            if (r < cumulative) {
                return i;
            }
        }
        return numberOfActions - 1; // Fallback in case of rounding error
    }

    // Update the joint Q-table using the observed joint action (own and opponent) and the received reward.
    @Override
    public void update(int own, int other, double reward) {
        // Standard Q-learning update for joint action (own, other)
        Q[own][other] = Q[own][other] + alpha * (reward - Q[own][other]);
        // Update opponent action counts for estimating opponent's behavior.
        oppCount[other]++;
        oppTotal++;
    }

    // For visualization, return the expected Q-value for action i.
    @Override
    public double getQ(int i) {
        double[] expected = getExpectedQ();
        return expected[i];
    }
}
