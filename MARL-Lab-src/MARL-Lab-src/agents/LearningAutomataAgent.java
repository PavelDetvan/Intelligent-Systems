package agents;

public class LearningAutomataAgent implements Agent {

    // Probability vector for each action (assuming two actions)
    private double[] prob;
    
    // Learning parameters for reward and penalty updates
    private double alpha;  // learning rate for good outcomes
    private double beta;   // learning rate for bad outcomes

    // Reward threshold to decide if an outcome is good or bad
    private double threshold;

    // Constructor: initialize equal probabilities and set parameters
    public LearningAutomataAgent(int numberOfActions) {
        prob = new double[numberOfActions];
        // Initialize to uniform probabilities
        for (int i = 0; i < numberOfActions; i++) {
            prob[i] = 1.0 / numberOfActions;
        }
        // Set learning rates (experiment with these values)
        alpha = 0.1;
        beta = 0.1;
        // Set a reward threshold (adjust according to your game payoffs)
        // Prisoners dilemma: -3
        // Matching pennies: 0
        // Battle of sexes: 1
        threshold = 1;
    }

    // Returns the probability of selecting action i
    @Override
    public double actionProb(int i) {
        return prob[i];
    }

    // Select an action according to the current probability distribution
    @Override
    public int selectAction() {
        double rand = Math.random();
        double cumulative = 0.0;
        for (int i = 0; i < prob.length; i++) {
            cumulative += prob[i];
            if (rand < cumulative) {
                return i;
            }
        }
        // Fallback
        return prob.length - 1;
    }

    // Update the probability vector based on the reward received.
    // In stateless games, we ignore the other agent's action.
    @Override
    public void update(int own, int other, double reward) {
        // Determine if the outcome is good or bad using the threshold.
        boolean goodOutcome = (reward >= threshold);

        // For each action, update the probabilities
        for (int i = 0; i < prob.length; i++) {
            if (i == own) {
                if (goodOutcome) {
                    // Increase probability for the chosen action
                    prob[i] = prob[i] + alpha * (1 - prob[i]);
                } else {
                    // Decrease probability for the chosen action
                    prob[i] = prob[i] - beta * prob[i];
                }
            } else {
                if (goodOutcome) {
                    // Decrease probability for the not chosen action
                    prob[i] = prob[i] - alpha * prob[i];
                } else {
                    // Increase probability for the not chosen action
                    prob[i] = prob[i] + beta * (1 - prob[i]);
                }
            }
        }
        // (Optional) You can re-normalize the probability vector to account for floating point errors.
        normalizeProbabilities();
    }

    // Utility method to re-normalize the probability vector (sums to 1)
    private void normalizeProbabilities() {
        double sum = 0.0;
        for (double p : prob) {
            sum += p;
        }
        for (int i = 0; i < prob.length; i++) {
            prob[i] /= sum;
        }
    }

    // For visualization, we can return the current probability as a "Q-value".
    @Override
    public double getQ(int i) {
        return prob[i];
    }
}
