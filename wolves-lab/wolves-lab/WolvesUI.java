import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class WolvesUI extends JPanel {

    private int squaresize;
    private Wolves game;
    
    public WolvesUI(Wolves game, int squaresize) {
        this.game = game;
        game.attach(this);
        this.squaresize = squaresize;
        setPreferredSize(new java.awt.Dimension(game.getNumbCols() * squaresize,
                                                  game.getNumbRows() * squaresize));
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        // Draw the grid background.
        drawgrid(g2);
        
        // ---------------------------
        // 1. Draw wolf visibility overlays.
        // ---------------------------
        // We'll assume that game.getWolfRows() and game.getWolfCols() return the positions (row and col).
        int[] wolfRows = game.getWolfRows();
        int[] wolfCols = game.getWolfCols();
        int numWolves = wolfRows.length;
        int numRows = game.getNumbRows();
        int numCols = game.getNumbCols();
        
        // Define an array of translucent colors for different wolves.
        Color[] wolfColors = new Color[] {
            new Color(255, 0, 0, 50),   // Red
            new Color(0, 255, 0, 50),   // Green
            new Color(0, 0, 255, 50),   // Blue
            new Color(255, 255, 0, 50), // Yellow
            new Color(0, 255, 255, 50)  // Cyan
        };
        
        // For each wolf, draw its visible area.
        for (int i = 0; i < numWolves; i++) {
            int wRow = wolfRows[i];
            int wCol = wolfCols[i];
            g2.setColor(wolfColors[i % wolfColors.length]);
            // Iterate over all cells in the grid.
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < numCols; c++) {
                    // Use the game’s manhattanDistance method.
                    int dist = game.manhattanDistance(wRow, wCol, r, c);
                    if (dist <= game.getVisibility()) {
                        // Draw a translucent rectangle over the cell.
                        g2.fill(new Rectangle2D.Double(c * squaresize + 1, r * squaresize + 1,
                                                         squaresize - 1, squaresize - 1));
                    }
                }
            }
        }
        
        // ---------------------------
        // 2. Draw the actual wolves and prey.
        // ---------------------------
        for (int i = 0; i < game.getNumbCols(); i++) {
            for (int j = 0; j < game.getNumbRows(); j++) {
                if (game.isWolf(i, j)) {
                    g2.setColor(Color.DARK_GRAY);
                    g2.fill(new Rectangle2D.Double(i * squaresize + 1, j * squaresize + 1,
                                                     squaresize - 1, squaresize - 1));
                } else if (game.isPrey(i, j)) {
                    g2.setColor(Color.YELLOW);
                    g2.fill(new Rectangle2D.Double(i * squaresize + 1, j * squaresize + 1,
                                                     squaresize - 1, squaresize - 1));
                }
            }
        }
        
        // ---------------------------
        // 3. Draw the locked target cell.
        // ---------------------------
        int[] lockedDir = LockedTargetWolf.getLockedDirection();
        if (lockedDir != null) {
            // Compute an average wolf position.
            int sumR = 0, sumC = 0;
            for (int i = 0; i < numWolves; i++) {
                sumR += wolfRows[i];
                sumC += wolfCols[i];
            }
            int avgR = sumR / numWolves;
            int avgC = sumC / numWolves;
            // For visualization, choose a factor (e.g., 3) so that the target cell is a few steps away.
            int factor = 3;
            int targetR = avgR + lockedDir[0] * factor;
            int targetC = avgC + lockedDir[1] * factor;
            
            // Wrap target coordinates (assuming torus behavior, using rowWrap and colWrap from game).
            targetR = game.rowWrap(avgR, lockedDir[0] * factor);
            targetC = game.colWrap(avgC, lockedDir[1] * factor);
            
            // Draw the target cell as a semi-transparent magenta overlay.
            g2.setColor(new Color(255, 0, 255, 150));
            g2.fill(new Rectangle2D.Double(targetC * squaresize + 1, targetR * squaresize + 1,
                                           squaresize - 1, squaresize - 1));
        }
    }
    
    public void drawgrid(Graphics2D g2) {
        // Fill background.
        g2.setColor(Color.LIGHT_GRAY);
        g2.fill(getVisibleRect());
        // Draw grid lines.
        g2.setColor(Color.GRAY);
        for (int i = 0; i <= game.getNumbCols(); i++)
            g2.drawLine(i * squaresize, 0, i * squaresize, game.getNumbRows() * squaresize);
        for (int j = 0; j <= game.getNumbRows(); j++)
            g2.drawLine(0, j * squaresize, game.getNumbCols() * squaresize, j * squaresize);
    }
    
    public void update() {
        repaint();
    }
}
