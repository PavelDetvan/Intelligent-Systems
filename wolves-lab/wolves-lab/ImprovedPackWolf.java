import java.util.List;
import java.util.Random;


/**
 * A wolf agent that either searches when alone or tries to form a pack
 * and hunt prey when it sees them. The wolf also uses a freeze mechanism
 * to avoid jitter if it gets too close to another wolf.
 */
public class ImprovedPackWolf implements Wolf {
    private Random rand = new Random();     // Random number generator used for movement decision

    private int freeze = 0;                // Freeze counter to avoid jitter when close to another wolf// Freeze counter to help avoid jitter when too close to pack members
    
    private int[] lastSearchDirection = {0, 0};     // The last search direction used when the wolf is alone. 
    
    /**
     * Function that computes the Manhattan distance between a wolf and another agent (wolf/prey) in relative coordinates.
     * 
     * @param x (wolfRow - otherRow)
     * @param y (wolfCol - otherCol)
     * @return The Manhattan distance = |x| + |y|
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
        // If freeze is active, stay put this tick.
        if (freeze > 0) {
            freeze--;
            return new int[]{0, 0};
        }
        
        // ----- SEARCH STATE -----
        // If no other wolf is visible, enter the search state.
        if (wolvesSight.isEmpty()) {
            // The wolf is alone, so it wanders around in search of others/prey
            return searchMove();
        }
        
        // ----- PACK FORMATION / HUNTING STATE -----
        // Find the nearest wolf, if we do see other wolves.
        int[] nearestWolf = null;
        int nearestWolfDistance = Integer.MAX_VALUE;
        for (int[] wolf : wolvesSight) {
            int d = manhattanDistance(wolf[0], wolf[1]);
            if (d < nearestWolfDistance) {
                nearestWolfDistance = d;
                nearestWolf = wolf;
            }
        }
        
        // If, for some reason, we couldnt find nearest wolf, just continue search.
        if (nearestWolf == null) {
            return searchMove();
        }
        
        // If prey are visible, choose the closest prey.
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
            
            // If the pack is already together, go hunting.
            if (nearestWolfDistance < 3) {
                // Instead of a direct chase, try to surround the prey a bit.
                int moveRow, moveCol;

                // Choose the best direction based on which difference is larger.
                if (Math.abs(targetPrey[0]) >= Math.abs(targetPrey[1])) {
                    // If the row difference is larger or equal, move in the row direction
                    moveRow = -Integer.signum(targetPrey[0]);
                    // If the prey is exactly in the same column, add a random offset horizontally
                    moveCol = (targetPrey[1] == 0) ? (rand.nextBoolean() ? 1 : -1) : 0;
                } else {
                    // If the column difference is larger, move in the column direction
                    moveCol = -Integer.signum(targetPrey[1]);
                    // If the prey is exactly in the same row, add a random offset vertically
                    moveRow = (targetPrey[0] == 0) ? (rand.nextBoolean() ? 1 : -1) : 0;
                }

                // Return the chosen move to surround prey
                return new int[]{moveRow, moveCol};
            } else {
                // If the pack isnt close enough even though prey are visible, focus on pack formation.
                int moveRow = -Integer.signum(nearestWolf[0]);
                int moveCol = -Integer.signum(nearestWolf[1]);
                return new int[]{moveRow, moveCol};
            }
        } else {
            // ----- PACK FORMATION (No Prey Visible) -----
            // When no prey are visible, move toward the nearest wolf.
            int moveRow = -Integer.signum(nearestWolf[0]);
            int moveCol = -Integer.signum(nearestWolf[1]);
            // If another wolf is very close, freeze for a bit to avoid jitter.
            if (nearestWolfDistance < 2) {
                freeze = rand.nextInt(5) + 1; // Freeze for 1 to 5 ticks
                return new int[]{0, 0};
            }
            return new int[]{moveRow, moveCol};
        }
    }
    
    /**
     * If the wolf sees no other wolves, it enters search state = keep moving in a stored direction, occasionally changing.
     */
    private int[] searchMove() {
        // If there is no current direction, choose one at random (but not stationary)
        if (lastSearchDirection[0] == 0 && lastSearchDirection[1] == 0) {
            lastSearchDirection[0] = rand.nextInt(3) - 1; // -1, 0, or 1
            lastSearchDirection[1] = rand.nextInt(3) - 1;
            if (lastSearchDirection[0] == 0 && lastSearchDirection[1] == 0) {
                lastSearchDirection[0] = 1; // Ensure a nonzero direction
            }
        } else {
            // Occasionally change direction randomly to explore
            if (rand.nextDouble() < 0.2) {  // 20% chance
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'moveLim'");
    }
}
