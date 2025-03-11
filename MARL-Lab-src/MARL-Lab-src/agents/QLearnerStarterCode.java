package agents;

public class QLearnerStarterCode implements Agent {

	private double Q[], alpha, alphadecay;
	// Depending on the exploration strategy you choose
	// you may need to add more parameters
	
	public QLearnerStarterCode(int numberOfActions) {
		Q = new double[numberOfActions];
		for (int i=0; i<numberOfActions; i++)
			// Initialize the Q-values to a random number between -0.1 and 0.1
			// You can choose other initialization strategies
			Q[i] = -0.1+Math.random()*0.2;
		alpha = 0.01;
		alphadecay = 1.0; // You can choose another decay rate
	}
	
	public double actionProb(int index) {
		return 0.5;
		// You need to implement this method
		// It should return the probability of selecting action index
		// given the current Q-values and the exploration strategy
	}
	
	public int selectAction() {
		return 0;
		// You need to implement this method
		// It should select an action according to the current Q-values
		// and the exploration strategy
	}
	
	public void update(int own, int other, double reward) {
		// Given that this is a simple Q-learner, and it doesn't 
		// take into account the actions of the other agent
		// you can ignore the other action here and instead implement:
		update(own,reward);
	}
	
	private void update(int index, double reward) {
		// You need to implement this method
		// It should update the Q-value for action index
		// using the reward received
		// ...

		// At the end, decay the learning rate
		alpha*=alphadecay;
	}

	@Override
	public double getQ(int i) {
		return Q[i];
	}

}
