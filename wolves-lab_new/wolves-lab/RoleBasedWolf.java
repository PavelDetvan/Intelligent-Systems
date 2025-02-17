import java.util.List;
import java.util.Random;

public class RoleBasedWolf implements Wolf{

    @Override
    public int[] moveAll(List<int[]> wolvesSight, List<int[]> preysSight) {
        int[] move = new int[2];
        
        // If no prey in sight, join the pack or explore
        if (preysSight.isEmpty()) {
            if (wolvesSight.isEmpty()) {
                // Explore randomly
                Random r = new Random();
                move[0] = r.nextInt(3) - 1;
                move[1] = r.nextInt(3) - 1;
                return move;
            }
            
            // Find the "center" of the wolf pack
            int sumX = 0, sumY = 0;
            for (int[] wolf : wolvesSight) {
                sumX += wolf[0];
                sumY += wolf[1];
            }
            int avgX = sumX / wolvesSight.size();
            int avgY = sumY / wolvesSight.size();
            
            // Move toward center of pack
            move[0] = avgX > 0 ? 1 : (avgX < 0 ? -1 : 0);
            move[1] = avgY > 0 ? 1 : (avgY < 0 ? -1 : 0);
            return move;
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
        
        // Determine role based on relative positions
        if (wolvesSight.size() > 0) {
            boolean amIClosest = true;
            boolean amIFarthest = true;
            int myDistance = Math.abs(closestPrey[0]) + Math.abs(closestPrey[1]);
            
            for (int[] wolf : wolvesSight) {
                int wolfDistance = Math.abs(wolf[0] - closestPrey[0]) + Math.abs(wolf[1] - closestPrey[1]);
                if (wolfDistance < myDistance) {
                    amIClosest = false;
                }
                if (wolfDistance > myDistance) {
                    amIFarthest = false;
                }
            }
            
            // Role: Closest wolf tries to go directly to prey
            if (amIClosest) {
                move[0] = closestPrey[0] > 0 ? 1 : (closestPrey[0] < 0 ? -1 : 0);
                move[1] = closestPrey[1] > 0 ? 1 : (closestPrey[1] < 0 ? -1 : 0);
            } 
            // Role: Farthest wolf tries to intercept
            else if (amIFarthest) {
                // Go to predicted position (simple prediction: double the offset)
                int targetX = closestPrey[0] * 2;
                int targetY = closestPrey[1] * 2;
                move[0] = targetX > 0 ? 1 : (targetX < 0 ? -1 : 0);
                move[1] = targetY > 0 ? 1 : (targetY < 0 ? -1 : 0);
            } 
            // Role: Middle wolf tries to flank
            else {
                // Determine which side to approach from
                boolean someoneLeft = false;
                boolean someoneRight = false;
                boolean someoneAbove = false;
                boolean someoneBelow = false;
                
                for (int[] wolf : wolvesSight) {
                    if (wolf[0] < closestPrey[0]) someoneLeft = true;
                    if (wolf[0] > closestPrey[0]) someoneRight = true;
                    if (wolf[1] < closestPrey[1]) someoneAbove = true;
                    if (wolf[1] > closestPrey[1]) someoneBelow = true;
                }
                
                if (!someoneLeft && closestPrey[0] > 0) {
                    move[0] = -1;
                } else if (!someoneRight && closestPrey[0] < 0) {
                    move[0] = 1;
                } else {
                    move[0] = closestPrey[0] > 0 ? 1 : (closestPrey[0] < 0 ? -1 : 0);
                }
                
                if (!someoneAbove && closestPrey[1] > 0) {
                    move[1] = -1;
                } else if (!someoneBelow && closestPrey[1] < 0) {
                    move[1] = 1;
                } else {
                    move[1] = closestPrey[1] > 0 ? 1 : (closestPrey[1] < 0 ? -1 : 0);
                }
            }
        } else {
            // No other wolves in sight, just move toward prey
            move[0] = closestPrey[0] > 0 ? 1 : (closestPrey[0] < 0 ? -1 : 0);
            move[1] = closestPrey[1] > 0 ? 1 : (closestPrey[1] < 0 ? -1 : 0);
        }
        
        return move;
    }

    @Override
    public int moveLim(List<int[]> wolvesSight, List<int[]> preysSight) {
        // Similar logic, but adapted for limited movement
        // Implementation follows the same pattern as moveAll with adjustments for limited directions
        
        // If no prey in sight, join the pack or explore
        if (preysSight.isEmpty()) {
            if (wolvesSight.isEmpty()) {
                // Explore randomly
                Random r = new Random();
                return r.nextInt(4) + 1;
            }
            
            // Find the "center" of the wolf pack
            int sumX = 0, sumY = 0;
            for (int[] wolf : wolvesSight) {
                sumX += wolf[0];
                sumY += wolf[1];
            }
            int avgX = sumX / wolvesSight.size();
            int avgY = sumY / wolvesSight.size();
            
            // Move toward center of pack (prioritize larger dimension)
            if (Math.abs(avgX) > Math.abs(avgY)) {
                if (avgX > 0) return 3; // right
                if (avgX < 0) return 1; // left
            } else {
                if (avgY > 0) return 2; // down
                if (avgY < 0) return 4; // up
            }
            return 0; // don't move
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
        
        // Determine role based on relative positions
        if (wolvesSight.size() > 0) {
            boolean amIClosest = true;
            boolean amIFarthest = true;
            int myDistance = Math.abs(closestPrey[0]) + Math.abs(closestPrey[1]);
            
            for (int[] wolf : wolvesSight) {
                int wolfDistance = Math.abs(wolf[0] - closestPrey[0]) + Math.abs(wolf[1] - closestPrey[1]);
                if (wolfDistance < myDistance) {
                    amIClosest = false;
                }
                if (wolfDistance > myDistance) {
                    amIFarthest = false;
                }
            }
            
            // Role: Closest wolf tries to go directly to prey
            if (amIClosest) {
                // Prioritize moving in the direction with larger distance
                if (Math.abs(closestPrey[0]) > Math.abs(closestPrey[1])) {
                    if (closestPrey[0] < 0) return 1; // left
                    if (closestPrey[0] > 0) return 3; // right
                } else {
                    if (closestPrey[1] < 0) return 4; // up
                    if (closestPrey[1] > 0) return 2; // down
                }
            } 
            // Role: Farthest wolf tries to intercept
            else if (amIFarthest) {
                // Go to predicted position (simple prediction: double the offset)
                int targetX = closestPrey[0] * 2;
                int targetY = closestPrey[1] * 2;
                
                if (Math.abs(targetX) > Math.abs(targetY)) {
                    if (targetX < 0) return 1; // left
                    if (targetX > 0) return 3; // right
                } else {
                    if (targetY < 0) return 4; // up
                    if (targetY > 0) return 2; // down
                }
            } 
            // Role: Middle wolf tries to flank
            else {
                // Determine which side to approach from
                boolean someoneLeft = false;
                boolean someoneRight = false;
                boolean someoneAbove = false;
                boolean someoneBelow = false;
                
                for (int[] wolf : wolvesSight) {
                    if (wolf[0] < closestPrey[0]) someoneLeft = true;
                    if (wolf[0] > closestPrey[0]) someoneRight = true;
                    if (wolf[1] < closestPrey[1]) someoneAbove = true;
                    if (wolf[1] > closestPrey[1]) someoneBelow = true;
                }
                
                if (!someoneLeft && closestPrey[0] > 0) {
                    return 1; // left
                } else if (!someoneRight && closestPrey[0] < 0) {
                    return 3; // right
                } else if (!someoneAbove && closestPrey[1] > 0) {
                    return 4; // up
                } else if (!someoneBelow && closestPrey[1] < 0) {
                    return 2; // down
                }
                
                // Default: move toward prey
                if (Math.abs(closestPrey[0]) > Math.abs(closestPrey[1])) {
                    if (closestPrey[0] < 0) return 1; // left
                    if (closestPrey[0] > 0) return 3; // right
                } else {
                    if (closestPrey[1] < 0) return 4; // up
                    if (closestPrey[1] > 0) return 2; // down
                }
            }
        }
        
        // No other wolves or no specific role behavior determined
        // Default: move toward prey
        if (Math.abs(closestPrey[0]) > Math.abs(closestPrey[1])) {
            if (closestPrey[0] < 0) return 1; // left
            if (closestPrey[0] > 0) return 3; // right
        } else {
            if (closestPrey[1] < 0) return 4; // up
            if (closestPrey[1] > 0) return 2; // down
        }
        
        // Default: don't move
        return 0;
    }
}
