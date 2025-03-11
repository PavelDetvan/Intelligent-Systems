package marlviz;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class PathViewer extends JPanel{
	
	private ArrayList<ArrayList<Double[]>> historyOfPaths;
	private ArrayList<Double[]> currentPath;
	private double scaleX, scaleY;
	
	final static float dash1[] = {10.0f};
    final static BasicStroke dashed =
        new BasicStroke(1.0f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        10.0f, dash1, 0.0f);
	
	public PathViewer() {
		historyOfPaths = new ArrayList<ArrayList<Double[]>>();
		currentPath = new ArrayList<Double[]>();
		updateScales();
		super.setBackground(Color.white);
	}
	
	public void addCoordinates(double[] ic) {
		Double[] nc = new Double[2];
		nc[0] = ic[0];
		nc[1] = ic[1];
		currentPath.add(nc);
	}
	
	public void newPath(){
		if (currentPath.size() > 0) {
			historyOfPaths.add(currentPath);
			currentPath = new ArrayList<Double[]>();
		}
	}
	
    private void updateScales() {
    	//scale makes the drawing grow/shrink as the window does
    	scaleX = getSize().getWidth()*0.8;
    	scaleY = getSize().getHeight()*0.8;
    }

    public Dimension getPreferredSize() {
    	return new Dimension(500,500);
    }
    
    private int getXFor(double val) {
    	return (int) (getSize().getWidth()*0.1+scaleX*val);
    }
    private int getYFor(double val) {
    	return (int) (getSize().getHeight() - (getSize().getHeight()*0.1+scaleY*val));
    }
    
	public void paintComponent(Graphics g) {
		updateScales();
		Graphics2D g2d = (Graphics2D)g;
		//Get rid of the old, it takes no time anyway ...
		g2d.setColor(super.getBackground());
		g2d.fillRect(0,0,(int)getSize().getWidth(),(int)getSize().getHeight());
		
		g2d.setStroke(new BasicStroke(1));
		g2d.setColor(Color.black);
		g2d.drawLine(getXFor(0), getYFor(0), getXFor(1), getYFor(0));
		g2d.drawLine(getXFor(0), getYFor(0), getXFor(0), getYFor(1));
		g2d.drawString("0", getXFor(0)-10, getYFor(0)+5);
		g2d.drawLine(getXFor(1), getYFor(1), getXFor(1), getYFor(0));
		g2d.drawLine(getXFor(1), getYFor(1), getXFor(0), getYFor(1));
		g2d.drawString("1", getXFor(1)+5, getYFor(1)+5);
		
		g2d.setStroke(dashed);
		for (int i=1; i<5; i++) {
			g2d.drawLine(getXFor(0), getYFor(i*0.2), getXFor(1), getYFor(i*0.2));
			g2d.drawLine(getXFor(i*0.2), getYFor(0), getXFor(i*0.2), getYFor(1));
		}
		
		g2d.setStroke(new BasicStroke(3));
		float hue = 0.0f;
		@SuppressWarnings("unchecked")
		ArrayList<ArrayList<Double[]>> drawThesePaths = (ArrayList<ArrayList<Double[]>>)historyOfPaths.clone();
		for (final ArrayList<Double[]> list : drawThesePaths) {
			double[] prev = {list.get(0)[0],list.get(0)[1]};
			hue += 0.3;
			hue -= (int) hue;
			g2d.setColor(Color.getHSBColor(hue, 1, 1));
			for (Double[] coord : list) {
				g2d.drawLine(getXFor(prev[0]),getYFor(prev[1]),getXFor(coord[0]),getYFor(coord[1]));
				prev[0] = coord[0];
				prev[1] = coord[1];
			}
		}
		
		g2d.setColor(Color.gray);
		if (currentPath.size()>0) {
			@SuppressWarnings("unchecked")
			ArrayList<Double[]> drawclone = (ArrayList<Double[]>)currentPath.clone();
			double[] prev = {drawclone.get(0)[0],drawclone.get(0)[1]};
			for (final Double[] coord : drawclone) {
				g2d.drawLine(getXFor(prev[0]),getYFor(prev[1]),getXFor(coord[0]),getYFor(coord[1]));
				prev[0] = coord[0];
				prev[1] = coord[1];
			}
			g2d.setColor(Color.red);
			g2d.drawOval(getXFor(drawclone.get(drawclone.size()-1)[0])-3, getYFor(drawclone.get(drawclone.size()-1)[1])-3, 6, 6);
		}
		
		g2d.setColor(Color.black);
		g2d.drawString("A", getXFor(-0.05), getYFor(0.57));
		g2d.drawString("g", getXFor(-0.05), getYFor(0.54));
		g2d.drawString("e", getXFor(-0.05), getYFor(0.51));
		g2d.drawString("n", getXFor(-0.05), getYFor(0.48));
		g2d.drawString("t", getXFor(-0.05), getYFor(0.45));
		g2d.drawString("2", getXFor(-0.05), getYFor(0.40));
		g2d.drawString("Agent 1", getXFor(0.45), getYFor(-0.05));

	}
}
