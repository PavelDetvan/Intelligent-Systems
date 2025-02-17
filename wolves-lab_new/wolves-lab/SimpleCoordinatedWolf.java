import java.util.List;
import java.util.Random;

public class SimpleCoordinatedWolf implements Wolf {
    private final Random random = new Random();
    
    @Override
    public int[] moveAll(List<int[]> wolvesSight, List<int[]> preysSight) {
        // If there's no prey in sight, follow other wolves or wander randomly
        if (preysSight.isEmpty()) {
            return followOtherWolvesOrWander(wolvesSight);
        }
        
        // Find the closest prey
        int[] closestPrey = findClosestPrey(preysSight);
        
        // Check if we're already adjacent to the prey
        if (isAdjacent(closestPrey)) {
            // Stay put if we're already adjacent to the prey
            return new int[]{0, 0};
        }
        
        // Move towards the closest prey
        return moveTowardsPrey(closestPrey);
    }
    
    private int[] followOtherWolvesOrWander(List<int[]> wolvesSight) {
        // If there are other wolves in sight, follow them
        if (!wolvesSight.isEmpty()) {
            // Find the wolf with the smallest Manhattan distance
            int[] closestWolf = wolvesSight.get(0);
            int minDistance = Math.abs(closestWolf[0]) + Math.abs(closestWolf[1]);
            
            for (int[] wolf : wolvesSight) {
                int distance = Math.abs(wolf[0]) + Math.abs(wolf[1]);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestWolf = wolf;
                }
            }
            
            // Move towards the closest wolf
            return moveTowards(closestWolf);
        }
        
        // If no wolves in sight, make a random move
        return makeRandomMove();
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
    
    private int[] moveTowardsPrey(int[] prey) {
        return moveTowards(prey);
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
    
    private int[] makeRandomMove() {
        return new int[]{random.nextInt(3) - 1, random.nextInt(3) - 1};
    }
    
    @Override
    public int moveLim(List<int[]> wolvesSight, List<int[]> preysSight) {
        // We'll focus on moveAll for this implementation
        // This method won't be called if limitMovement is false
        return 0;
    }
}