package marlviz;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class PathWindow extends JFrame {


	private PathViewer view;

	public PathWindow(String title) {
		this.setTitle(title);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Add a PathViewer
		view = new PathViewer();
		this.add(view, BorderLayout.CENTER);

		//Open in center of screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = view.getPreferredSize(); 
 		int left = (screenSize.width - frameSize.width)/ 2;
		int top  = (screenSize.height - frameSize.height)/ 2;
		this.setLocation(left, top);
		
		this.pack();
		this.setVisible(true);
	}

	public void addCoordinates(double[] nc) {
		view.addCoordinates(nc);
		paintPaths();
	}
	
	public void newPath(){
		view.newPath();
	}

	public void paintPaths() {
		view.repaint();
	}
	
}
