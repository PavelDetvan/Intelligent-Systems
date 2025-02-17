import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FollowPrey implements Wolf {

    @Override
    public int[] moveAll(List<int[]> wolvesSight, List<int[]> preysSight) {
        int[] move = new int[2];
        
        // If no prey in sight, move randomly
        if (preysSight.isEmpty()) {
            Random r = new Random();
            move[0] = r.nextInt(3) - 1;
            move[1] = r.nextInt(3) - 1;
            return move;
        }
        
        // Find the closest prey
        int[] closestPrey = preysSight.get(0);
        System.out.println(closestPrey);
        int minDistance = Math.abs(closestPrey[0]) + Math.abs(closestPrey[1]);
        
        for (int[] prey : preysSight) {
            int distance = Math.abs(prey[0]) + Math.abs(prey[1]);
            if (distance < minDistance) {
                minDistance = distance;
                closestPrey = prey;
            }
        }
        
        // Move toward the closest prey (basic approach)
        move[0] = closestPrey[0] > 0 ? 1 : (closestPrey[0] < 0 ? -1 : 0);
        move[1] = closestPrey[1] > 0 ? 1 : (closestPrey[1] < 0 ? -1 : 0);
        
        return move;
    }

    @Override
    public int moveLim(List<int[]> wolvesSight, List<int[]> preysSight) {
        // If no prey in sight, move randomly
        if (preysSight.isEmpty()) {
            Random r = new Random();
            return r.nextInt(4) + 1;
        }
        
        // Find the closest prey
        int[] closestPrey = preysSight.get(0);
        int minDistance = Math.abs(closestPrey[0]) + Math.abs(closestPrey[1]);
        
        for (int[] prey : preysSight) {
            int distance = Math.abs(prey[0]) + Math.abs(prey[1]);
            if (distance < minDistance) {
                minDistance = distance;
                closestPrey = prey;
            }
        }
        
        // Move toward the closest prey
        if (Math.abs(closestPrey[0]) > Math.abs(closestPrey[1])) {
            // Move horizontally first
            if (closestPrey[0] < 0) return 1; // left
            if (closestPrey[0] > 0) return 3; // right
        } else {
            // Move vertically first
            if (closestPrey[1] < 0) return 4; // up
            if (closestPrey[1] > 0) return 2; // down
        }
        
        // Default: don't move
        return 0;
    }
    
}
