package games;

public class PrisonersDilemma implements Game {

	@Override
	public double[] move(int first, int second) {
		if (first==0 && second==0) {
			double[] res = {-3.0,-3.0};
			return res;
		} else if (first==1 && second==1){
			double[] res = {-1.0,-1.0};
			return res;
		} else if (first==0 && second==1){
			double[] res = {0.0,-5.0};
			return res;
		} else {
			double[] res = {-5.0,0.0};
			return res;
		}

	}

}
