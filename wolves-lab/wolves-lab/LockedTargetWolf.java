import java.util.List;
import java.util.Random;

public class LockedTargetWolf implements Wolf {

    // Shared locked direction representing the target prey.
    // Once set, it remains until we determine that prey is no longer visible (i.e. assumed captured).
    private static int[] lockedDirection = null;
    
    private Random random = new Random();

    /**
     * moveAll implements the following logic:
     * 
     * 1. If a target is already locked, and at least one prey is visible,
     *    check if any of those prey (converted to a unit direction) match the locked direction.
     *    - If none match (and prey are visible), assume the locked prey was captured and clear the lock.
     * 2. If no target is locked (or if the lock was just cleared) and prey are visible,
     *    lock onto the closest prey by computing its unit direction relative to the current wolf.
     * 3. If no prey are visible and no target is locked, move randomly.
     * 4. Once a target is locked, return the same locked direction regardless of the wolf’s own view.
     */
    @Override
    public int[] moveAll(List<int[]> wolvesSight, List<int[]> preysSight) {
        // If a target is locked and we have some prey in view, check if the target is still visible.
        if (lockedDirection != null && !preysSight.isEmpty()) {
            boolean targetStillVisible = false;
            // For each prey in view, compute its unit direction.
            for (int[] prey : preysSight) {
                int uRow = Integer.compare(prey[0], 0);
                int uCol = Integer.compare(prey[1], 0);
                // If one of the visible prey has the same unit direction as our lock, we assume the target is still there.
                if (uRow == lockedDirection[0] && uCol == lockedDirection[1]) {
                    targetStillVisible = true;
                    break;
                }
            }
            // If prey are visible but none match the locked target, assume the target was captured.
            if (!targetStillVisible) {
                lockedDirection = null;
            }
        }
        
        // If no target is locked, try to lock one if any prey are visible.
        if (lockedDirection == null) {
            if (!preysSight.isEmpty()) {
                // Select the closest prey (using Manhattan distance)
                int[] targetPrey = selectClosestPrey(preysSight);
                int unitRow = Integer.compare(targetPrey[0], 0);
                int unitCol = Integer.compare(targetPrey[1], 0);
                lockedDirection = new int[] { unitRow, unitCol };
                return lockedDirection;
            } else {
                // No prey visible and no target locked: move randomly.
                return new int[] { random.nextInt(3) - 1, random.nextInt(3) - 1 };
            }
        }
        
        // If a target is locked, always move in that direction.
        return new int[] { lockedDirection[0], lockedDirection[1] };
    }

    /**
     * moveLim converts the two-component move from moveAll into a single integer
     * (with 0 = no move, 1 = left, 2 = down, 3 = right, 4 = up).
     * (Note: if both coordinates are nonzero, we choose the one with the larger magnitude.)
     */
    @Override
    public int moveLim(List<int[]> wolvesSight, List<int[]> preysSight) {
        int[] move = moveAll(wolvesSight, preysSight);
        if (move[0] != 0 && move[1] != 0) {
            if (Math.abs(move[0]) >= Math.abs(move[1])) {
                move[1] = 0;
            } else {
                move[0] = 0;
            }
        }
        if (move[0] == 0 && move[1] == 0) return 0;
        if (move[0] != 0) {
            if (move[0] == -1) return 4; // up
            if (move[0] == 1)  return 2; // down
        }
        if (move[1] != 0) {
            if (move[1] == -1) return 1; // left
            if (move[1] == 1)  return 3; // right
        }
        return 0; // fallback (should not occur)
    }

    /**
     * Helper method to select the closest prey from the visible list (using Manhattan distance).
     */
    private int[] selectClosestPrey(List<int[]> preys) {
        int[] best = preys.get(0);
        int bestDist = Math.abs(best[0]) + Math.abs(best[1]);
        for (int[] p : preys) {
            int d = Math.abs(p[0]) + Math.abs(p[1]);
            if (d < bestDist) {
                best = p;
                bestDist = d;
            }
        }
        return best;
    }

    public static int[] getLockedDirection() {
        return lockedDirection;
    }
    
}
