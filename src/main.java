import java.awt.*;

public class main
{
    public static void main(String args[]) {
	CustomFrame f = new CustomFrame("Project A", 800, 500);
	f.setLayout(new BorderLayout());

	StatusBar status = new StatusBar(800,30);
	f.add(status, "North");	

	DrawingArea d = new DrawingArea(800,410, status);
	f.add(d, "Center");	

//	AndrewConvexHull algor = null;
//	algor = new AndrewConvexHull(d);
	TSP algor = null;
	algor = new TSP(d);
	algor.addAlgorithmStatusListener(status);

	Controls cntl = new Controls(d);
	cntl.setAlgorithm(algor);
	f.add(cntl, "South");

	f.validate();
	f.pack();
	f.setVisible(true);

	Image img = f.createImage(d.getPreferredSize().width, 
				  d.getPreferredSize().height);
	Graphics gc  = img.getGraphics();
	d.setImageAndGC(img, gc);

	if(algor != null)
	    algor.start();
    }
}
