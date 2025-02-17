import java.util.List;
import java.util.Random;

public class PackHuntingWolf implements Wolf {

    // Shared target: once locked, all wolves chase this target (represented as a unit direction vector)
    private static int[] lockedTarget = null;
    
    private Random random = new Random();

    /**
     * The moveAll method implements the following logic:
     * 
     * 1. If a target is already locked, check if at least one prey in view still seems to match it.
     *    - If yes, continue chasing that locked target.
     *    - If not (and if the pack is grouped), assume it’s been captured and clear the lock.
     * 
     * 2. If no target is locked:
     *    a. If the pack is not grouped (wolves are spread out), move toward the pack’s center.
     *    b. If the pack is grouped and at least one prey is visible, lock onto the closest prey
     *       (by storing its unit direction) and use that as the move.
     * 
     * 3. If no prey is visible, simply group (or return a small random move to avoid getting stuck).
     */
    @Override
    public int[] moveAll(List<int[]> wolvesSight, List<int[]> preysSight) {
        // 1. Check if we have a locked target.
        if (lockedTarget != null) {
            boolean targetVisible = false;
            // Check among visible prey if any have the same unit direction as the locked target.
            for (int[] prey : preysSight) {
                int preyDirRow = Integer.compare(prey[0], 0);
                int preyDirCol = Integer.compare(prey[1], 0);
                if (preyDirRow == lockedTarget[0] && preyDirCol == lockedTarget[1]) {
                    targetVisible = true;
                    break;
                }
            }
            // If the pack is grouped but no one sees the locked target, assume it was captured.
            if (!targetVisible && isPackGrouped(wolvesSight)) {
                lockedTarget = null;
            }
            // If still locked, continue chasing.
            if (lockedTarget != null) {
                return new int[] { lockedTarget[0], lockedTarget[1] };
            }
        }
        
        // 2. No locked target exists.
        // a) If the pack is not grouped, move toward the center of the pack.
        if (!isPackGrouped(wolvesSight)) {
            int sumRow = 0, sumCol = 0;
            if (!wolvesSight.isEmpty()) {
                for (int[] w : wolvesSight) {
                    sumRow += w[0];
                    sumCol += w[1];
                }
                // Compute the average relative position.
                int avgRow = (int) Math.round((double) sumRow / wolvesSight.size());
                int avgCol = (int) Math.round((double) sumCol / wolvesSight.size());
                // Move in the direction that reduces the difference.
                int moveRow = Integer.compare(avgRow, 0);
                int moveCol = Integer.compare(avgCol, 0);
                return new int[] { moveRow, moveCol };
            } else {
                // No other wolves visible, so pick a random move.
                return new int[] { random.nextInt(3) - 1, random.nextInt(3) - 1 };
            }
        }
        
        // b) Pack is grouped and no target is locked. If prey is visible, lock onto the closest one.
        if (!preysSight.isEmpty()) {
            int[] targetPrey = selectClosestPrey(preysSight);
            int preyDirRow = Integer.compare(targetPrey[0], 0);
            int preyDirCol = Integer.compare(targetPrey[1], 0);
            // Lock onto this target. All wolves will now use this direction.
            lockedTarget = new int[] { preyDirRow, preyDirCol };
            return new int[] { preyDirRow, preyDirCol };
        }
        
        // 3. Fallback: if no prey is visible even when grouped, try a slight random move.
        int fallbackRow = random.nextInt(3) - 1;
        int fallbackCol = random.nextInt(3) - 1;
        return new int[] { fallbackRow, fallbackCol };
    }

    /**
     * For limited (cardinal-only) movement, we call moveAll and then remove any diagonal components.
     * The conversion is:
     *   0 = no move, 1 = left, 2 = down, 3 = right, 4 = up.
     */
    @Override
    public int moveLim(List<int[]> wolvesSight, List<int[]> preysSight) {
        int[] move = moveAll(wolvesSight, preysSight);
        // Remove diagonal if necessary: pick the component with the larger absolute value.
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
     * Determines if the pack is well grouped based on the average Manhattan distance of nearby wolves.
     * Adjust the threshold (here set to ≤ 2) as needed.
     */
    private boolean isPackGrouped(List<int[]> wolvesSight) {
        if (wolvesSight.isEmpty()) return false;
        double totalDist = 0;
        for (int[] w : wolvesSight) {
            totalDist += (Math.abs(w[0]) + Math.abs(w[1]));
        }
        double avgDist = totalDist / wolvesSight.size();
        return avgDist <= 2;  // Threshold; tweak if needed.
    }
    
    /**
     * Helper method to select the closest prey (by Manhattan distance).
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
}
