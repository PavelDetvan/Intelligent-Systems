package games;

public class MatchingPennies implements Game {

	@Override
	public double[] move(int first, int second) {
		if (first==second) {
			double[] res = {1.0,-1.0};
			return res;
		} else {
			double[] res = {-1.0,1.0};
			return res;
		}
	}

}
