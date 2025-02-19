import java.util.List;
import java.util.Random;

/**
 * A wolf agent that either searches when alone or tries to form a pack
 * and hunt prey when it sees them. Instead of freezing when very close,
 * I do a small random offset to avoid jitter but keep them moving.
 */
public class ImprovedPackWolfNoFreeze implements Wolf {

    private Random rand = new Random();   // Random number generator used for movement decision
    
    private int[] lastSearchDirection = {0, 0};  // The last search direction used when the wolf is alone
    
    /**
     * Computes Manhattan distance
     * @param x Relative row difference
     * @param y Relative column difference
     * @return |x| + |y|
     */
    private int manhattanDistance(int x, int y) {
        return Math.abs(x) + Math.abs(y);
    }
    
    /**
     * Main movement method for all movement (including diagonals).
     * Here we decide if the wolf will search (if alone), form a pack, or hunt prey based on what it sees.
     * 
     * @param wolvesSight A list of relative positions of other wolves 
     * @param preysSight  A list of relative positions of any visible prey
     * @return An int[] of length 2 indicating row movement and column movement (each element can be -1, 0, or 1)
     */
    @Override
    public int[] moveAll(List<int[]> wolvesSight, List<int[]> preysSight) {
        // If no other wolf is visible enter search state
        if (wolvesSight.isEmpty()) {
            return searchMove();
        }
        
        // Find the nearest wolf.
        int[] nearestWolf = null;
        int nearestWolfDistance = Integer.MAX_VALUE;
        for (int[] wolf : wolvesSight) {
            int d = manhattanDistance(wolf[0], wolf[1]);
            if (d < nearestWolfDistance) {
                nearestWolfDistance = d;
                nearestWolf = wolf;
            }
        }
        
        // If no nearest wolf found for some reason, keep searching.
        if (nearestWolf == null) {
            return searchMove();
        }
        
        // If preys are  visible, choose the closest prey and decide whether to hunt or form pack.
        if (!preysSight.isEmpty()) {
            int[] targetPrey = null;
            int nearestPreyDistance = Integer.MAX_VALUE;
            for (int[] prey : preysSight) {
                int d = manhattanDistance(prey[0], prey[1]);
                if (d < nearestPreyDistance) {
                    nearestPreyDistance = d;
                    targetPrey = prey;
                }
            }
            
            // If pack is close (distance < 3), try to hunt/surround the prey.
            if (nearestWolfDistance < 3) {
                return surroundMove(targetPrey);
            } else {
                // If not close to pack, move toward the nearest wolf first.
                return gatherMove(nearestWolf);
            }
        } else {
            // If no prey visible, focus on pack formation.
            // Instead of freezing if distance < 2, do a small offset to avoid jitter.
            if (nearestWolfDistance < 2) {
                // Make a small random move to avoid standing still or jitter.
                // random direction in [-1, 0, 1] for row & col
                int randRow = rand.nextInt(3) - 1; // -1, 0, 1
                int randCol = rand.nextInt(3) - 1;
                return new int[]{randRow, randCol};
            } else {
                // Otherwise move to the nearest wolf to form pack
                return gatherMove(nearestWolf);
            }
        }
    }
    
    /**
     * Returns movement in a direction that tries to surround the prey rather than direct chasing.
     */
    private int[] surroundMove(int[] targetPrey) {
        int moveRow, moveCol;
        
        // Compare absolute row vs. column difference to pick direction.
        if (Math.abs(targetPrey[0]) >= Math.abs(targetPrey[1])) {
            moveRow = -Integer.signum(targetPrey[0]);
            // If prey is in the same column, add random horizontal offset
            moveCol = (targetPrey[1] == 0) ? (rand.nextBoolean() ? 1 : -1) : 0;
        } else {
            moveCol = -Integer.signum(targetPrey[1]);
            // If prey is in the same row, add random vertical offset
            moveRow = (targetPrey[0] == 0) ? (rand.nextBoolean() ? 1 : -1) : 0;
        }
        return new int[]{moveRow, moveCol};
    }
    
    /**
     * Returns move toward the nearest wolf to form pack.
     */
    private int[] gatherMove(int[] nearestWolf) {
        int moveRow = -Integer.signum(nearestWolf[0]);
        int moveCol = -Integer.signum(nearestWolf[1]);
        return new int[]{moveRow, moveCol};
    }

    /**
     * If the wolf sees no other wolves, it enters search state,
     * moving in its stored direction that changes occasionally.
     */
    private int[] searchMove() {
        // If no direction yet, pick a random one (non zero)
        if (lastSearchDirection[0] == 0 && lastSearchDirection[1] == 0) {
            lastSearchDirection[0] = rand.nextInt(3) - 1; 
            lastSearchDirection[1] = rand.nextInt(3) - 1;
            if (lastSearchDirection[0] == 0 && lastSearchDirection[1] == 0) {
                lastSearchDirection[0] = 1; 
            }
        } else {
            // 20% chance to pick a new random direction
            if (rand.nextDouble() < 0.2) {
                lastSearchDirection[0] = rand.nextInt(3) - 1;
                lastSearchDirection[1] = rand.nextInt(3) - 1;
                if (lastSearchDirection[0] == 0 && lastSearchDirection[1] == 0) {
                    lastSearchDirection[0] = 1;
                }
            }
        }
        return new int[]{lastSearchDirection[0], lastSearchDirection[1]};
    }
    
    @Override
    public int moveLim(List<int[]> wolvesSight, List<int[]> preysSight) {
        throw new UnsupportedOperationException("Unimplemented method 'moveLim'");
    }
}
