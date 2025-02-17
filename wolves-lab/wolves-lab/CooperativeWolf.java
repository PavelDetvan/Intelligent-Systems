import java.util.List;
import java.util.Random;

public class CooperativeWolf implements Wolf {

    private Random random = new Random();

    /**
     * This method implements movement when diagonal moves are allowed.
     * The strategy is:
     *   - If any prey is visible, select the closest prey and compute a direction toward it.
     *   - Also compute a "group" direction from the positions of nearby wolves.
     *   - Combine the prey direction (weighted more) with the group direction so that all wolves
     *     converge on the same target and move as a pack.
     *   - If no prey is visible, simply move toward the center of the pack.
     *   - If no information is available or the result is (0,0), choose a random move.
     */
    @Override
    public int[] moveAll(List<int[]> wolvesSight, List<int[]> preysSight) {
        int rowMove = 0, colMove = 0;
        
        // Weight factors: prey direction is more important than group direction.
        double preyWeight = 1.0;
        double groupWeight = 0.5;
        
        // Compute prey direction if any prey is visible.
        boolean hasPrey = false;
        int preyDirRow = 0, preyDirCol = 0;
        if (!preysSight.isEmpty()) {
            int[] targetPrey = selectClosestPrey(preysSight);
            // The prey is given in relative coordinates (how far away it is).
            preyDirRow = Integer.compare(targetPrey[0], 0);
            preyDirCol = Integer.compare(targetPrey[1], 0);
            hasPrey = true;
        }
        
        // Compute group (pack) direction from nearby wolves.
        int groupDirRow = 0, groupDirCol = 0;
        if (!wolvesSight.isEmpty()) {
            int sumRow = 0, sumCol = 0;
            for (int[] w : wolvesSight) {
                sumRow += w[0];
                sumCol += w[1];
            }
            // The sign of the sum tells us which direction most wolves are relative to us.
            groupDirRow = Integer.compare(sumRow, 0);
            groupDirCol = Integer.compare(sumCol, 0);
        }
        
        // Combine the signals:
        // If prey is visible, we bias our move toward it (while still trying to keep the pack together).
        if (hasPrey) {
            double combinedRow = preyWeight * preyDirRow + groupWeight * groupDirRow;
            double combinedCol = preyWeight * preyDirCol + groupWeight * groupDirCol;
            rowMove = Integer.compare((int) Math.round(combinedRow), 0);
            colMove = Integer.compare((int) Math.round(combinedCol), 0);
        } else {
            // If no prey is visible, simply move toward the pack center.
            rowMove = groupDirRow;
            colMove = groupDirCol;
        }
        
        // If the computed move is zero (or gets stuck), choose a random move.
        if (rowMove == 0 && colMove == 0) {
            rowMove = random.nextInt(3) - 1;
            colMove = random.nextInt(3) - 1;
        }
        
        // Return the move, which is one step in each coordinate (each in {-1, 0, 1}).
        return new int[]{rowMove, colMove};
    }

    /**
     * For limited (cardinal-only) movement, we reuse moveAll and then remove any diagonal component.
     * The conversion is as follows:
     * 0 = no move, 1 = left, 2 = down, 3 = right, 4 = up.
     */
    @Override
    public int moveLim(List<int[]> wolvesSight, List<int[]> preysSight) {
        int[] move = moveAll(wolvesSight, preysSight);
        // If both coordinates are non-zero, choose one based on which absolute value is larger.
        if (move[0] != 0 && move[1] != 0) {
            if (Math.abs(move[0]) >= Math.abs(move[1])) {
                move[1] = 0;
            } else {
                move[0] = 0;
            }
        }
        // Convert the (row, col) move to a single integer:
        // 0 = no move, 1 = left, 2 = down, 3 = right, 4 = up.
        if (move[0] == 0 && move[1] == 0) return 0; // no movement

        if (move[0] != 0) {
            if (move[0] == -1) return 4; // up
            if (move[0] == 1)  return 2; // down
        }
        if (move[1] != 0) {
            if (move[1] == -1) return 1; // left
            if (move[1] == 1)  return 3; // right
        }
        return 0; // fallback (should not reach here)
    }

    /**
     * Helper method to select the closest prey from the list based on Manhattan distance.
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
