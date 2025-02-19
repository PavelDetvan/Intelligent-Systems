import java.util.List;
import java.util.Random;

public class ImprovedPackWolf2 implements Wolf {

    private Random rand = new Random();
    // (We no longer use freeze to stop movement in the pack phase.)
    private int freeze = 0;
    // Last search direction for when the wolf is alone (search state)
    private int[] lastSearchDirection = {0, 0};
    // Common pack movement direction when the pack is together and no prey is visible.
    private int[] packDirection = {0, 0};
    private boolean packDirectionInitialized = false;
    
    // Helper: Manhattan distance on the relative coordinate system.
    private int manhattanDistance(int x, int y) {
        return Math.abs(x) + Math.abs(y);
    }
    
    @Override
    public int[] moveAll(List<int[]> wolvesSight, List<int[]> preysSight) {
        System.out.println("----- New moveAll tick -----");
        System.out.println("wolvesSight size: " + wolvesSight.size());
        System.out.println("preysSight size: " + preysSight.size());
        
        // If freeze is active, stay put this tick.
        // (You could remove this if you don't want any freeze at all.)
        if (freeze > 0) {
            freeze--;
            System.out.println("Freeze active. Staying still. Freeze left: " + freeze);
            return new int[]{0, 0};
        }
        
        // ----- SEARCH STATE -----
        // If no other wolf is visible, enter the search state.
        if (wolvesSight.isEmpty()) {
            System.out.println("No wolves in sight: entering SEARCH state.");
            // Reset common pack direction because we're alone.
            packDirectionInitialized = false;
            return searchMove();
        }
        
        // ----- PACK FORMATION / HUNTING STATE -----
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
        System.out.println("Nearest wolf: " + arrayToString(nearestWolf) + ", Distance: " + nearestWolfDistance);
        
        // If for some reason we couldn't find a nearest wolf, switch to search.
        if (nearestWolf == null) {
            System.out.println("Nearest wolf is null. Entering SEARCH state.");
            return searchMove();
        }
        
        // If prey are visible, choose the closest prey.
        if (!preysSight.isEmpty()) {
            // Reset the common pack direction when prey is seen.
            packDirectionInitialized = false;
            
            int[] targetPrey = null;
            int nearestPreyDistance = Integer.MAX_VALUE;
            for (int[] prey : preysSight) {
                int d = manhattanDistance(prey[0], prey[1]);
                if (d < nearestPreyDistance) {
                    nearestPreyDistance = d;
                    targetPrey = prey;
                }
            }
            System.out.println("Target prey chosen: " + arrayToString(targetPrey) + ", Distance: " + nearestPreyDistance);
            
            // If the pack is together (i.e. nearest wolf is close enough), go hunting.
            if (nearestWolfDistance < 3) {
                System.out.println("Pack is together. Entering HUNT state.");
                // Move toward the prey: invert the prey's relative vector.
                int moveRow = -Integer.signum(targetPrey[0]);
                int moveCol = -Integer.signum(targetPrey[1]);
                
                // Optionally add a slight random offset (20% chance) to help with surrounding.
                if (rand.nextDouble() < 0.2) {
                    moveRow += (rand.nextBoolean() ? 1 : -1);
                    moveCol += (rand.nextBoolean() ? 1 : -1);
                    System.out.println("Adding slight random offset for surrounding.");
                }
                System.out.println("HUNT move: [" + moveRow + ", " + moveCol + "]");
                return new int[]{moveRow, moveCol};
            } else {
                // Pack is not tight enough: gather by moving toward the nearest wolf.
                System.out.println("Prey visible but pack not formed. Focusing on PACK FORMATION (gathering).");
                int moveRow = -Integer.signum(nearestWolf[0]);
                int moveCol = -Integer.signum(nearestWolf[1]);
                System.out.println("Gathering move: [" + moveRow + ", " + moveCol + "]");
                return new int[]{moveRow, moveCol};
            }
        } else {
            // ----- PACK FORMATION (No Prey Visible) -----
            System.out.println("No prey visible. Focusing on PACK FORMATION.");
            // Now, if the pack is already tight, use common pack movement.
            // If the pack is not tight, gather by moving toward the nearest wolf.
            if (nearestWolfDistance < 2) {
                // Pack is tight. Use common movement.
                if (!packDirectionInitialized) {
                    packDirection[0] = rand.nextInt(3) - 1;
                    packDirection[1] = rand.nextInt(3) - 1;
                    // Ensure a nonzero direction.
                    if (packDirection[0] == 0 && packDirection[1] == 0) {
                        packDirection[0] = 1;
                    }
                    packDirectionInitialized = true;
                    System.out.println("Initializing pack direction: " + arrayToString(packDirection));
                } else {
                    System.out.println("Using existing pack direction: " + arrayToString(packDirection));
                }
                return new int[]{packDirection[0], packDirection[1]};
            } else {
                // Not yet tight, so move toward the nearest wolf to gather.
                int moveRow = -Integer.signum(nearestWolf[0]);
                int moveCol = -Integer.signum(nearestWolf[1]);
                System.out.println("Gathering move: [" + moveRow + ", " + moveCol + "]");
                return new int[]{moveRow, moveCol};
            }
        }
    }
    
    /**
     * Implements a search movement strategy for when the wolf is alone.
     * The wolf continues in its last search direction, with occasional random changes.
     */
    private int[] searchMove() {
        if (lastSearchDirection[0] == 0 && lastSearchDirection[1] == 0) {
            lastSearchDirection[0] = rand.nextInt(3) - 1;
            lastSearchDirection[1] = rand.nextInt(3) - 1;
            if (lastSearchDirection[0] == 0 && lastSearchDirection[1] == 0) {
                lastSearchDirection[0] = 1;
            }
            System.out.println("SEARCH: New random direction: " + arrayToString(lastSearchDirection));
        } else {
            if (rand.nextDouble() < 0.2) {
                lastSearchDirection[0] = rand.nextInt(3) - 1;
                lastSearchDirection[1] = rand.nextInt(3) - 1;
                if (lastSearchDirection[0] == 0 && lastSearchDirection[1] == 0) {
                    lastSearchDirection[0] = 1;
                }
                System.out.println("SEARCH: Changed direction randomly to: " + arrayToString(lastSearchDirection));
            } else {
                System.out.println("SEARCH: Continuing with direction: " + arrayToString(lastSearchDirection));
            }
        }
        return new int[]{lastSearchDirection[0], lastSearchDirection[1]};
    }
    
    // Helper to print array values nicely.
    private String arrayToString(int[] arr) {
        if (arr == null) return "null";
        return "[" + arr[0] + ", " + arr[1] + "]";
    }

    @Override
    public int moveLim(List<int[]> wolvesSight, List<int[]> preysSight) {
        throw new UnsupportedOperationException("Unimplemented method 'moveLim'");
    }
}
