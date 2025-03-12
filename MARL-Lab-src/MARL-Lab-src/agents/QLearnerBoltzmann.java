package agents;

public class QLearnerBoltzmann implements Agent {

    private double[] Q;
    private double alpha;
    private double alphadecay;
    private double tau;  // Temperature parameter for Boltzmann exploration

    // Constructor: Initialize Q-values, learning rate, decay, and temperature
    public QLearnerBoltzmann(int numberOfActions) {
        Q = new double[numberOfActions];
        for (int i = 0; i < numberOfActions; i++) {
            // Initialize Q-values to small random numbers near zero
            Q[i] = -0.1 + Math.random() * 0.2;
        }
        alpha = 0.01;       // Learning rate
        alphadecay = 1.0;   // Decay factor for learning rate
        tau = 0.2;          // Temperature parameter for exploration 
    }
    
    // Helper: Compute the sum of exponentials for normalization
    private double computeNormalization() {
        double sum = 0.0;
        for (double q : Q) {
            sum += Math.exp(q / tau);
        }
        return sum;
    }

    @Override
    public double actionProb(int i) {
        double norm = computeNormalization();
        return Math.exp(Q[i] / tau) / norm;
    }

    @Override
    public int selectAction() {
        // Compute probabilities for all actions
        double norm = computeNormalization();
        double[] probs = new double[Q.length];
        for (int i = 0; i < Q.length; i++) {
            probs[i] = Math.exp(Q[i] / tau) / norm;
        }
        // Sample action based on probabilities
        double rand = Math.random();
        double cumulative = 0.0;
        for (int i = 0; i < probs.length; i++) {
            cumulative += probs[i];
            if (rand < cumulative) {
                return i;
            }
        }
        // In case of rounding errors, return the last action
        return Q.length - 1;
    }

    @Override
    public void update(int own, int other, double reward) {
        Q[own] = Q[own] + alpha * (reward - Q[own]);
        alpha *= alphadecay;
    }

    @Override
    public double getQ(int i) {
        return Q[i];
    }
}

