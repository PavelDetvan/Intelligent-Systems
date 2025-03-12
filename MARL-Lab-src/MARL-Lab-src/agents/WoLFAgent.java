package agents;

import java.util.Arrays;

public class WoLFAgent implements Agent {

    // Number of available actions (assumed to be 2 in our case)
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
        
        // Set learning rates (tune these parameters experimentally)
        alpha = 0.01;
        deltaW = 0.01;  // When winning: slow update
        deltaL = 0.04;  // When losing: faster update
    }

    // Returns the probability of selecting action i (for visualization)
    @Override
    public double actionProb(int i) {
        return policy[i];
    }

    // Selects an action by sampling from the current policy distribution
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

    // Update the agent based on the chosen action, opponent's action (ignored here), and received reward
    @Override
    public void update(int own, int other, double reward) {
        // 1. Update Q-value for the chosen action
        Q[own] = Q[own] + alpha * (reward - Q[own]);
        
        // 2. Evaluate current expected value under current policy: V_current = Σ π(i)*Q(i)
        double V_current = 0.0;
        for (int i = 0; i < numActions; i++) {
            V_current += policy[i] * Q[i];
        }
        
        // 3. Evaluate expected value under average policy: V_avg = Σ π_avg(i)*Q(i)
        double V_avg = 0.0;
        for (int i = 0; i < numActions; i++) {
            V_avg += policyAvg[i] * Q[i];
        }
        
        // 4. Determine which learning rate to use
        double delta = (V_current > V_avg) ? deltaW : deltaL;
        
        // 5. Identify the best action according to current Q-values
        int bestAction = 0;
        double maxQ = Q[0];
        for (int i = 1; i < numActions; i++) {
            if (Q[i] > maxQ) {
                maxQ = Q[i];
                bestAction = i;
            }
        }
        
        // 6. Policy Hill Climbing Update (WoLF update):
        // For best action: increase probability; for others: decrease probability.
        double[] newPolicy = Arrays.copyOf(policy, numActions);
        for (int i = 0; i < numActions; i++) {
            if (i == bestAction) {
                newPolicy[i] = policy[i] + delta * (1 - policy[i]);
            } else {
                newPolicy[i] = policy[i] - delta * policy[i];
            }
        }
        // Normalize newPolicy to ensure it sums to 1 (should be nearly 1 by construction)
        double sum = 0.0;
        for (double p : newPolicy) {
            sum += p;
        }
        for (int i = 0; i < numActions; i++) {
            newPolicy[i] /= sum;
        }
        policy = newPolicy;
        
        // 7. Update average policy (running average over time)
        for (int i = 0; i < numActions; i++) {
            policyAvg[i] = policyAvg[i] + (1.0 / t) * (policy[i] - policyAvg[i]);
        }
        t++;  // Increment time step counter
    }

    // For visualization: return the expected Q-value for action i (i.e., π(i)*Q(i) summed over actions)
    @Override
    public double getQ(int i) {
        return Q[i];
    }
}

