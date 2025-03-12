import agents.*;
import games.*;
import marlviz.*;

public class Main {

	public static void main(String args[]) {
		
		
		PathWindow pw = new PathWindow("Action Probabilities for action 0");
		ValueWindow vw1 = new ValueWindow("QValues Agent 0");
		ValueWindow vw2 = new ValueWindow("QValues Agent 1");
		
		//Game game = new PrisonersDilemma();
		//Game game = new MatchingPennies();
		Game game = new BattleOfSexes();
		
		int cnt = 0;
		for (int i=0; i<5; i++) {
			Agent agent1 = new WoLFAgent(2);
			Agent agent2 = new WoLFAgent(2);
			while (cnt < 5000) {
				int a1 = agent1.selectAction();
				int a2 = agent2.selectAction();
				double[] rewards = game.move(a1,a2);
				agent1.update(a1,a2,rewards[0]);
				agent2.update(a2,a1,rewards[1]);
				double[] nc = {agent1.actionProb(0),agent2.actionProb(0)};
				pw.addCoordinates(nc);
				double[] nv = {agent1.getQ(0), agent1.getQ(1)};
				vw1.addValues(nv);
				double[] nv2 = {agent2.getQ(0), agent2.getQ(1)};
				vw2.addValues(nv2);
				cnt++;
				// To slow down the visualization. 
				// If removed, the end result is show pretty much instantly. 
				try {Thread.sleep(4);} catch (InterruptedException e) {}
			}
			pw.newPath();
			vw1.newPath();
			vw2.newPath();
			cnt = 0;
		}
			
	}
	
}
