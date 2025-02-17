import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CoordinatedWolf implements Wolf {
    private final Random random = new Random();
    private static final int PROXIMITY_THRESHOLD = 2; // How close wolves should be to coordinate
    
    @Override
    public int[] moveAll(List<int[]> wolvesSight, List<int[]> preysSight) {
        // If there's no prey in sight, follow other wolves or explore
        if (preysSight.isEmpty()) {
            return followOtherWolvesOrExplore(wolvesSight);
        }
        
        // Find the closest prey
        int[] closestPrey = findClosestPrey(preysSight);
        
        // Check if we're already adjacent to the prey
        if (isAdjacent(closestPrey)) {
            // Stay put if we're already adjacent to the prey
            return new int[]{0, 0};
        }
        
        // Check if other wolves are close to the prey - if yes, prioritize this prey
        List<int[]> wolvesNearPrey = findWolvesNearTarget(wolvesSight, closestPrey, PROXIMITY_THRESHOLD);
        
        if (!wolvesNearPrey.isEmpty()) {
            // Move directly towards the prey for coordinated attack
            return moveTowards(closestPrey);
        }
        
        // No wolves near the prey yet, check if we're the closest wolf to the prey
        if (amIClosestWolfToPrey(wolvesSight, closestPrey)) {
            return moveTowards(closestPrey);
        }
        
        // If we're not the closest, follow other wolves that might be closer
        int[] wolfToFollow = findWolfClosestToPrey(wolvesSight, closestPrey);
        if (wolfToFollow != null) {
            return moveTowards(wolfToFollow);
        }
        
        // Fallback: move towards prey
        return moveTowards(closestPrey);
    }
    
    private int[] followOtherWolvesOrExplore(List<int[]> wolvesSight) {
        // If there are other wolves in sight, follow them
        if (!wolvesSight.isEmpty()) {
            int[] closestWolf = findClosestWolf(wolvesSight);
            
            // If wolf is far away, move towards it
            int wolfDistance = Math.abs(closestWolf[0]) + Math.abs(closestWolf[1]);
            if (wolfDistance > 2) {
                return moveTowards(closestWolf);
            }
            
            // If wolf is already close, move in similar direction to maintain formation
            return moveInFormation(closestWolf);
        }
        
        // If no wolves in sight, make a strategic random move
        return makeStrategicMove();
    }
    
    private int[] findClosestWolf(List<int[]> wolvesSight) {
        int[] closestWolf = wolvesSight.get(0);
        int minDistance = Math.abs(closestWolf[0]) + Math.abs(closestWolf[1]);
        
        for (int[] wolf : wolvesSight) {
            int distance = Math.abs(wolf[0]) + Math.abs(wolf[1]);
            if (distance < minDistance) {
                minDistance = distance;
                closestWolf = wolf;
            }
        }
        
        return closestWolf;
    }
    
    private int[] findClosestPrey(List<int[]> preysSight) {
        int[] closestPrey = preysSight.get(0);
        int minDistance = Math.abs(closestPrey[0]) + Math.abs(closestPrey[1]);
        
        for (int[] prey : preysSight) {
            int distance = Math.abs(prey[0]) + Math.abs(prey[1]);
            if (distance < minDistance) {
                minDistance = distance;
                closestPrey = prey;
            }
        }
        
        return closestPrey;
    }
    
    private boolean isAdjacent(int[] prey) {
        return Math.abs(prey[0]) <= 1 && Math.abs(prey[1]) <= 1;
    }
    
    private List<int[]> findWolvesNearTarget(List<int[]> wolves, int[] target, int threshold) {
        List<int[]> wolvesNearTarget = new ArrayList<>();
        
        for (int[] wolf : wolves) {
            // Calculate wolf's distance to the target
            int wolfToTargetRow = wolf[0] - target[0];
            int wolfToTargetCol = wolf[1] - target[1];
            int distance = Math.abs(wolfToTargetRow) + Math.abs(wolfToTargetCol);
            
            if (distance <= threshold) {
                wolvesNearTarget.add(wolf);
            }
        }
        
        return wolvesNearTarget;
    }
    
    private boolean amIClosestWolfToPrey(List<int[]> wolves, int[] prey) {
        int myDistance = Math.abs(prey[0]) + Math.abs(prey[1]);
        
        for (int[] wolf : wolves) {
            int wolfToPreyRow = wolf[0] - prey[0];
            int wolfToPreyCol = wolf[1] - prey[1];
            int wolfDistance = Math.abs(wolfToPreyRow) + Math.abs(wolfToPreyCol);
            
            if (wolfDistance < myDistance) {
                return false; // Found a wolf closer to the prey
            }
        }
        
        return true; // I am the closest wolf to the prey
    }
    
    private int[] findWolfClosestToPrey(List<int[]> wolves, int[] prey) {
        if (wolves.isEmpty()) return null;
        
        int[] closestWolf = wolves.get(0);
        int minDistance = Integer.MAX_VALUE;
        
        for (int[] wolf : wolves) {
            int wolfToPreyRow = wolf[0] - prey[0];
            int wolfToPreyCol = wolf[1] - prey[1];
            int distance = Math.abs(wolfToPreyRow) + Math.abs(wolfToPreyCol);
            
            if (distance < minDistance) {
                minDistance = distance;
                closestWolf = wolf;
            }
        }
        
        return closestWolf;
    }
    
    private int[] moveInFormation(int[] wolf) {
        // Move in a similar direction to maintain pack formation
        // but with slight variation to avoid collisions
        int rowMove = wolf[0] == 0 ? 0 : (wolf[0] > 0 ? 1 : -1);
        int colMove = wolf[1] == 0 ? 0 : (wolf[1] > 0 ? 1 : -1);
        
        // Add small random variation to avoid perfect overlap
        if (random.nextDouble() < 0.3) {
            rowMove = random.nextInt(3) - 1;
        }
        if (random.nextDouble() < 0.3) {
            colMove = random.nextInt(3) - 1;
        }
        
        return new int[]{rowMove, colMove};
    }
    
    private int[] moveTowards(int[] target) {
        int rowMove = 0;
        int colMove = 0;
        
        // Move in the row direction
        if (target[0] < 0) rowMove = -1;
        else if (target[0] > 0) rowMove = 1;
        
        // Move in the column direction
        if (target[1] < 0) colMove = -1;
        else if (target[1] > 0) colMove = 1;
        
        return new int[]{rowMove, colMove};
    }
    
    private int[] makeStrategicMove() {
        // Strategic random movement:
        // 80% chance to move in a cardinal direction (N, S, E, W)
        // 20% chance to move diagonally
        if (random.nextDouble() < 0.8) {
            // Cardinal direction
            int direction = random.nextInt(4);
            switch (direction) {
                case 0: return new int[]{-1, 0}; // North
                case 1: return new int[]{1, 0};  // South
                case 2: return new int[]{0, -1}; // West
                case 3: return new int[]{0, 1};  // East
            }
        }
        
        // Diagonal movement
        int rowMove = random.nextBoolean() ? 1 : -1;
        int colMove = random.nextBoolean() ? 1 : -1;
        return new int[]{rowMove, colMove};
    }
    
    @Override
    public int moveLim(List<int[]> wolvesSight, List<int[]> preysSight) {
        // We'll focus on moveAll for this implementation
        return 0;
    }
}