import java.applet.*;
import java.awt.*;

public class convexhullapplet extends Applet
{
    Controls cntl;

    public void init()
    {
	setLayout(new BorderLayout());
	
	StatusBar status = new StatusBar(600,30);
	add(status, "North");	

	DrawingArea d = new DrawingArea(600,410, status);
	add(d, "Center");	

//	AndrewConvexHull algor = new AndrewConvexHull(d);
	TSP algor = new TSP(d);
	algor.addAlgorithmStatusListener(status);

	cntl = new Controls(d);
	cntl.setAlgorithm(algor);
	add(cntl, "South");

	validate();
	setVisible(true);
	
	Image img = createImage(d.getPreferredSize().width, 
				d.getPreferredSize().height);
	Graphics gc  = img.getGraphics();
	d.setImageAndGC(img, gc);

	if(algor != null)
	    algor.start();
    }

    public void destroy() 
    {
	System.out.println("destroy");
	super.destroy();
    }
    
    public void start()
    {
	System.out.println("start");
	super.start();
	if(cntl != null)
	    cntl.resetEverything();
    }


    public void stop()
    {
	System.out.println("stop");
	super.stop();
	if(cntl != null)
	    cntl.resetEverything();
    }
    
}
