package agents;

public interface Agent {

	// Returns the probability of selecting action i
	// This is used for the visualization of the policies of the agents
	public abstract double actionProb(int i);
	
	// Selects an action according to the agent's policy
	public abstract int selectAction();
	
	// Updates the agent's Q-values
	// own is the action the agent took
	// other is the action the other agent took
	// reward is the reward received
	public abstract void update(int own, int other, double reward);

	// Returns the Q-value of action i
	// This is used for the visualization of the Q-values
	public abstract double getQ(int i);
	
}
