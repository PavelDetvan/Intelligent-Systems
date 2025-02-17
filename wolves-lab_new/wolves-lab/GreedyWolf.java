import java.util.List;
import java.util.Random;

public class GreedyWolf implements Wolf {

    private Random rand = new Random();

    @Override
    public int[] moveAll(List<int[]> wolvesSight, List<int[]> preysSight) {
        // If one or more preys are visible, chase the closest one.
        if (!preysSight.isEmpty()) {
            int[] targetPrey = findClosestPrey(preysSight);
            // Determine row movement: -1 if prey is above, 1 if below, 0 if aligned.
            int moveRow = (targetPrey[0] < 0) ? -1 : (targetPrey[0] > 0) ? 1 : 0;
            // Determine column movement: -1 if prey is to the left, 1 if to the right, 0 if aligned.
            int moveCol = (targetPrey[1] < 0) ? -1 : (targetPrey[1] > 0) ? 1 : 0;
            return new int[]{moveRow, moveCol};
        }
        
        // If no prey is visible, try to coordinate with nearby wolves.
        if (!wolvesSight.isEmpty()) {
            int[] avgDirection = computeAverageDirection(wolvesSight);
            int moveRow = (avgDirection[0] < 0) ? -1 : (avgDirection[0] > 0) ? 1 : 0;
            int moveCol = (avgDirection[1] < 0) ? -1 : (avgDirection[1] > 0) ? 1 : 0;
            
            // If by chance the computed direction is (0,0), add a small random move
            if (moveRow == 0 && moveCol == 0) {
                moveRow = rand.nextInt(3) - 1;
                moveCol = rand.nextInt(3) - 1;
            }
            return new int[]{moveRow, moveCol};
        }
        
        // If nothing is in sight, choose a random small move.
        return new int[]{rand.nextInt(3) - 1, rand.nextInt(3) - 1};
    }

    /**
     * Returns the prey (from the list of visible preys) that has the smallest Manhattan distance.
     */
    private int[] findClosestPrey(List<int[]> preysSight) {
        int minDistance = Integer.MAX_VALUE;
        int[] closest = null;
        for (int[] prey : preysSight) {
            int distance = Math.abs(prey[0]) + Math.abs(prey[1]);
            if (distance < minDistance) {
                minDistance = distance;
                closest = prey;
            }
        }
        return closest;
    }

    /**
     * Computes an "average" direction based on the positions of nearby wolves.
     * Since each wolf is represented by a relative position [relRow, relCol],
     * summing these up and then taking an average gives a rough direction to the pack's center.
     */
    private int[] computeAverageDirection(List<int[]> wolvesSight) {
        int sumRow = 0;
        int sumCol = 0;
        for (int[] otherWolf : wolvesSight) {
            sumRow += otherWolf[0];
            sumCol += otherWolf[1];
        }
        int count = wolvesSight.size();
        // Note: averaging here gives a directional bias. In many cases, rounding toward zero is enough.
        return new int[]{sumRow / count, sumCol / count};
    }

    @Override
    public int moveLim(List<int[]> wolvesSight, List<int[]> preysSight) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'moveLim'");
    }
}
