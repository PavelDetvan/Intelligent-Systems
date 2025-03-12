package agents;

import java.util.Arrays;

public class WoLFAgent implements Agent {
    private int numActions;
    // Estimated Q-values for each action
    private double[] Q;
    // Current policy: probability distribution over actions
    private double[] policy;
    // Average policy (running average of past policies)
    private double[] policyAvg;
    // Learning rates for Q-value update and policy update
    private double alpha;   // Q-learning update rate
    private double deltaW;  // Small policy update rate (when winning)
    private double deltaL;  // Large policy update rate (when losing)
    // Time step counter for updating average policy
    private int t;

    // Constructor: initialize all parameters and policies
    public WoLFAgent(int numActions) {
        this.numActions = numActions;
        Q = new double[numActions];
        policy = new double[numActions];
        policyAvg = new double[numActions];
        t = 1;  // Start at 1 to avoid division by zero
        
        // Initialize Q-values to small random numbers
        for (int i = 0; i < numActions; i++) {
            Q[i] = -0.1 + Math.random() * 0.2;
        }
        
        // Initialize policy uniformly
        for (int i = 0; i < numActions; i++) {
            policy[i] = 1.0 / numActions;
            policyAvg[i] = 1.0 / numActions;
        }
        
        // Set learning rates
        alpha = 0.01;
        deltaW = 0.01;  // When winning: slow update
        deltaL = 0.04;  // When losing: faster update
    }

    // Returns the probability of selecting action i (for visualization)
    @Override
    public double actionProb(int i) {
        return policy[i];
    }

    // Selects action by sampling from current policy distribution
    @Override
    public int selectAction() {
        double rand = Math.random();
        double cumulative = 0.0;
        for (int i = 0; i < numActions; i++) {
            cumulative += policy[i];
            if (rand < cumulative) {
                return i;
            }
        }
        return numActions - 1;  // Fallback in case of rounding error
    }

    // Update agent based on chosen action, and received reward
    @Override
    public void update(int own, int other, double reward) {
        // Update Q-value for the chosen action
        Q[own] = Q[own] + alpha * (reward - Q[own]);
        
        // Evaluate current expected value under current policy: V_current = SUM pi(i)*Q(i)
        double V_current = 0.0;
        for (int i = 0; i < numActions; i++) {
            V_current += policy[i] * Q[i];
        }
        
        // Evaluate expected value under average policy: V_avg = SUM pi_avg(i)*Q(i)
        double V_avg = 0.0;
        for (int i = 0; i < numActions; i++) {
            V_avg += policyAvg[i] * Q[i];
        }
        
        // Determine which learning rate to use
        double delta = (V_current > V_avg) ? deltaW : deltaL;
        
        // Identify the best action according to current Q-values
        int bestAction = 0;
        double maxQ = Q[0];
        for (int i = 1; i < numActions; i++) {
            if (Q[i] > maxQ) {
                maxQ = Q[i];
                bestAction = i;
            }
        }
        
        // Policy Hill Climbing Update (WoLF update):
        // For best action: increase probability; for others: decrease probability
        double[] newPolicy = Arrays.copyOf(policy, numActions);
        for (int i = 0; i < numActions; i++) {
            if (i == bestAction) {
                newPolicy[i] = policy[i] + delta * (1 - policy[i]);
            } else {
                newPolicy[i] = policy[i] - delta * policy[i];
            }
        }
        // Normalize newPolicy to ensure it sums to 1
        double sum = 0.0;
        for (double p : newPolicy) {
            sum += p;
        }
        for (int i = 0; i < numActions; i++) {
            newPolicy[i] /= sum;
        }
        policy = newPolicy;
        
        // Update average policy (running average over time)
        for (int i = 0; i < numActions; i++) {
            policyAvg[i] = policyAvg[i] + (1.0 / t) * (policy[i] - policyAvg[i]);
        }
        t++;  // Increment time step counter
    }

    @Override
    public double getQ(int i) {
        return Q[i];
    }
}

