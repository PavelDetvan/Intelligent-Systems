import java.util.List;
import java.util.Random;

/**
 * GroupRoleWolf implements a coordinated behavior that combines group movement
 * with role differentiation.
 *
 * Strategy:
 * 1. If no prey is visible, wander randomly.
 * 2. If a prey is visible:
 *    - If the prey is farther than 2 steps away, move directly toward it (Pursuing Role).
 *    - If the prey is very close (Manhattan distance <= 2), move perpendicular to the 
 *      direct vector (Blocking/Flanking Role) so that wolves can approach from different angles.
 * 3. Adjust the move if other wolves are too close (repulsion).
 * 4. Normalize the final move to ensure each component is -1, 0, or 1.
 * 5. If no clear move results, choose a random move.
 */
public class GroupRoleWolf implements Wolf {

    @Override
    public int[] moveAll(List<int[]> wolvesSight, List<int[]> preysSight) {
        int[] move = new int[2]; // move[0]: row change, move[1]: column change
        move[0] = 0;
        move[1] = 0;
        Random r = new Random();

        // --- Step 1: Prey Handling ---
        if (preysSight.isEmpty()) {
            // No prey in sight – wander randomly.
            move[0] = r.nextInt(3) - 1; // random value from -1, 0, or 1
            move[1] = r.nextInt(3) - 1;
        } else {
            // Identify the closest prey using Manhattan distance.
            int[] targetPrey = null;
            int minDistance = Integer.MAX_VALUE;
            for (int[] p : preysSight) {
                int distance = Math.abs(p[0]) + Math.abs(p[1]);
                if (distance < minDistance) {
                    minDistance = distance;
                    targetPrey = p;
                }
            }
            if (targetPrey != null) {
                // --- Step 2: Role Differentiation Based on Distance ---
                if (minDistance <= 2) {
                    // Blocking/Flanking Role:
                    // Instead of moving directly toward the prey, move perpendicular.
                    // Compute the direct vector from wolf to prey.
                    int dx = targetPrey[0];
                    int dy = targetPrey[1];
                    
                    // One perpendicular vector is (dy, -dx). (You might also choose (-dy, dx).)
                    int[] blockVector = new int[2];
                    blockVector[0] = dy;
                    blockVector[1] = -dx;
                    
                    // Normalize the blocking vector to values in {-1, 0, 1}.
                    if (blockVector[0] > 0)
                        blockVector[0] = 1;
                    else if (blockVector[0] < 0)
                        blockVector[0] = -1;
                    if (blockVector[1] > 0)
                        blockVector[1] = 1;
                    else if (blockVector[1] < 0)
                        blockVector[1] = -1;
                    
                    move[0] = blockVector[0];
                    move[1] = blockVector[1];
                } else {
                    // Pursuing Role: move directly toward the prey.
                    int dx = targetPrey[0];
                    int dy = targetPrey[1];
                    // Normalize each component.
                    if (dx > 0)
                        dx = 1;
                    else if (dx < 0)
                        dx = -1;
                    if (dy > 0)
                        dy = 1;
                    else if (dy < 0)
                        dy = -1;
                    
                    move[0] = dx;
                    move[1] = dy;
                }
            }
        }
        
        // --- Step 3: Repulsion from Nearby Wolves ---
        // For every other wolf in sight that is in one of the adjacent cells, adjust the move.
        for (int[] otherWolf : wolvesSight) {
            if (Math.abs(otherWolf[0]) <= 1 && Math.abs(otherWolf[1]) <= 1) {
                // If the other wolf is above, push down; if below, push up.
                if (otherWolf[0] < 0)
                    move[0] += 1;
                else if (otherWolf[0] > 0)
                    move[0] -= 1;
                
                // If the other wolf is to the left, push right; if to the right, push left.
                if (otherWolf[1] < 0)
                    move[1] += 1;
                else if (otherWolf[1] > 0)
                    move[1] -= 1;
            }
        }
        
        // --- Step 4: Normalize the Move ---
        if (move[0] > 0)
            move[0] = 1;
        else if (move[0] < 0)
            move[0] = -1;
        
        if (move[1] > 0)
            move[1] = 1;
        else if (move[1] < 0)
            move[1] = -1;
        
        // --- Step 5: Fallback ---
        // If the move is [0,0] (no clear direction), pick a random move.
        if (move[0] == 0 && move[1] == 0) {
            move[0] = r.nextInt(3) - 1;
            move[1] = r.nextInt(3) - 1;
        }
        
        return move;
    }

    /**
     * The moveLim method is used when only cardinal moves (no diagonals) are allowed.
     * It converts the result of moveAll into a single integer:
     * 0 = no move, 1 = left, 2 = down, 3 = right, 4 = up.
     */
    @Override
    public int moveLim(List<int[]> wolvesSight, List<int[]> preysSight) {
        int[] move = moveAll(wolvesSight, preysSight);
        // If the move is diagonal (both components non-zero), favor vertical movement.
        if (move[0] != 0 && move[1] != 0) {
            move[1] = 0;
        }
        
        // Map the move vector to the required integer.
        if (move[0] == 0 && move[1] == 0)
            return 0;
        if (move[0] == 1 && move[1] == 0)
            return 2;  // down
        if (move[0] == -1 && move[1] == 0)
            return 4;  // up
        if (move[0] == 0 && move[1] == 1)
            return 3;  // right
        if (move[0] == 0 && move[1] == -1)
            return 1;  // left
        
        // Fallback: no movement.
        return 0;
    }
}
