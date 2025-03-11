package agents;

public class FixedAgent implements Agent {

	// Works only for two actions ... sue me
	// The agent does not learn, it is fixed
	// The agent does not have Q-values
	// The agent does not update its Q-values
	// The agent does not update its policy
	// The agent does not have a policy
	// The agent does not have a learning rate
	// The agent does not have a discount factor
	// The agent does not have a temperature
	// The agent does not have a softmax policy
	// The agent does not have a Boltzmann policy
	// The agent does not have a greedy policy
	// The agent does not have an epsilon-greedy policy
	// It is a very boring agent

	double firstactionprob = 0.5;
	
	@Override

	// The probability of selecting the first action is given by firstactionprob
	// The second action is selected with probability 1-firstactionprob	
	public double actionProb(int i) {
		if (i==0)
			return firstactionprob;
		else
			return (1-firstactionprob);
					
	}

	@Override
	// Returns 0 or 1 (representing the actions) 
	// with probability firstactionprob or 1-firstactionprob respectively
	public int selectAction() {
		if (Math.random() < firstactionprob) return 0;
		else return 1;
	}

	@Override
	public void update(int own, int other, double reward) {
		// nothing to see here
		// only a fixed agent here
	}

	@Override
	public double getQ(int i) {
		// doesn't do Q-values ...
		return 0;
	}
	
	
	

}
