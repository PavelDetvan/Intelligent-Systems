package marlviz;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ValueViewer extends JPanel {
	
	private ArrayList<ArrayList<Double[]>> historyOfPaths;
	private ArrayList<Double[]> currentPath;
	private double scaleX, scaleY, minValue, maxValue;
	private int maxEntries;
	
	final static float[] dashB = {10.0f};
    final static BasicStroke dashedBig =
            new BasicStroke(1.0f,
                            BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_MITER,
                            10.0f, dashB, 0.0f);
	final static float[] dashS = {2.0f};
    final static BasicStroke dashedSmall =
            new BasicStroke(2.0f,
                            BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_MITER,
                            10.0f, dashS, 0.0f);
	
	public ValueViewer() {
		historyOfPaths = new ArrayList<ArrayList<Double[]>>();
		currentPath = new ArrayList<Double[]>();
		super.setBackground(Color.white);
		double[] init = {-0.1, 0.1};
		updateScales(init, 10);
	}
	
	public void addValues(double[] ic) {
		Double[] nc = new Double[2];
		nc[0] = ic[0];
		nc[1] = ic[1];
		currentPath.add(nc);
		updateScales(ic,currentPath.size());
	}
	
	public void newPath(){
		if (currentPath.size() > 0) {
			historyOfPaths.add(currentPath);
			currentPath = new ArrayList<Double[]>();
		}
	}
	
    private void updateScales(double[] ic, int size) {
    	//scale makes the drawing grow/shrink as the window does
    	maxEntries = Math.max(maxEntries, size);
    	scaleX = getSize().getWidth()*0.8/maxEntries;
    	minValue = Math.min(minValue, Math.min(ic[0], ic[1]));
    	maxValue = Math.max(maxValue, Math.max(ic[0], ic[1]));
    	scaleY = getSize().getHeight()*0.8/(maxValue-minValue);
    }

    public Dimension getPreferredSize() {
    	return new Dimension(1000,500);
    }
    
    private int getXFor(double val) {
    	return (int) (getSize().getWidth()*0.1+scaleX*val);
    }
    private int getYFor(double val) {
    	return (int) (getSize().getHeight()*0.1+scaleY*(maxValue-val));
    }
    
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		//Get rid of the old
		g2d.setColor(super.getBackground());
		g2d.fillRect(0,0,(int)getSize().getWidth(),(int)getSize().getHeight());
		
		g2d.setStroke(new BasicStroke(1));
		g2d.setColor(Color.black);
		g2d.drawLine(getXFor(0), getYFor(0), getXFor(maxEntries), getYFor(0));
		g2d.drawString("Time", getXFor(maxEntries)+20, getYFor(0)-5);
		g2d.drawString("Q-value", getXFor(0)-60, getYFor(maxValue)-20);
		g2d.drawLine(getXFor(0), getYFor(minValue*1.05), getXFor(0), getYFor(maxValue*1.05));	
		g2d.setStroke(new BasicStroke(2));
		g2d.drawLine(getXFor(0)+10,getYFor(maxValue)-25,getXFor(0)+35,getYFor(maxValue)-25);
		g2d.drawString("Action 0", getXFor(0)+40, getYFor(maxValue)-20);
		g2d.setStroke(dashedSmall);
		g2d.drawLine(getXFor(0)+110,getYFor(maxValue)-25,getXFor(0)+135,getYFor(maxValue)-25);
		g2d.drawString("Action 1", getXFor(0)+140, getYFor(maxValue)-20);		
		
		g2d.setStroke(dashedBig);
		for (int y = (int) (minValue*2.1); y<maxValue*2.1; y++) {
			g2d.drawLine(getXFor(0), getYFor(y/2.0), getXFor(maxEntries), getYFor(y/2.0));
			g2d.drawString(String.valueOf(y/2.0), getXFor(0)-20, getYFor(y/2.0));
		}
		
		float hue = 0.0f;
		@SuppressWarnings("unchecked")
		ArrayList<ArrayList<Double[]>> drawThesePaths = (ArrayList<ArrayList<Double[]>>)historyOfPaths.clone();
		for (final ArrayList<Double[]> list : drawThesePaths) {
			double[] prev = {list.get(0)[0],list.get(0)[1]};
			hue += 0.3;
			hue -= (int) hue;
			g2d.setColor(Color.getHSBColor(hue, 1, 1));
			int x = 1;
			for (final Double[] coord : list) {
				g2d.setStroke(new BasicStroke(2));
				g2d.drawLine(getXFor(x-1),getYFor(prev[0]),getXFor(x),getYFor(coord[0]));
				g2d.setStroke(dashedSmall);
				g2d.drawLine(getXFor(x-1),getYFor(prev[1]),getXFor(x),getYFor(coord[1]));
				prev[0] = coord[0];
				prev[1] = coord[1];
				x++;
			}
		}
		
		g2d.setColor(Color.gray);
		if (currentPath.size()>0) {
			@SuppressWarnings("unchecked")
			ArrayList<Double[]> drawclone = (ArrayList<Double[]>)currentPath.clone();
			double[] prev = {drawclone.get(0)[0],drawclone.get(0)[1]};
			int x = 1;
			for (final Double[] coord : drawclone) {
				g2d.setStroke(new BasicStroke(2));
				g2d.drawLine(getXFor(x-1),getYFor(prev[0]),getXFor(x),getYFor(coord[0]));
				g2d.setStroke(dashedSmall);
				g2d.drawLine(getXFor(x-1),getYFor(prev[1]),getXFor(x),getYFor(coord[1]));
				prev[0] = coord[0];
				prev[1] = coord[1];
				x++;
			}
		}
	}

}
