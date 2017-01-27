import java.awt.*;
import java.awt.event.*;

class Controls extends Panel 
    implements ActionListener, AlgorithmStatusListener
{
    DrawingArea drawArea;
    Button reset, polyMode, close;
    Button resetAlgor, exec, suspend, animate, algorMode;
    Algorithm algor;

    public Controls(DrawingArea d)
    {
	drawArea = d;
	Util.Assert(d != null, "Controls()");

	reset = new Button("reset everything");
	reset.setActionCommand(reset.getLabel());
	reset.addActionListener(this);
	add(reset);

	polyMode = new Button("polygon mode");
	polyMode.setActionCommand(polyMode.getLabel());
	polyMode.addActionListener(this);
	add(polyMode);

	close = new Button("close polygon");
	close.setActionCommand(close.getLabel());
	close.addActionListener(this);
	add(close);

	algorMode = new Button("algorithm");
	algorMode.setActionCommand(algorMode.getLabel());
	algorMode.addActionListener(this);
	add(algorMode);

	exec = new Button("execute");
	exec.setActionCommand(exec.getLabel());
	exec.addActionListener(this);
	add(exec);

	suspend = new Button("suspend");
	suspend.setActionCommand(suspend.getLabel());
	suspend.addActionListener(this);
	add(suspend);

	animate = new Button("animate");
	animate.setActionCommand(animate.getLabel());
	animate.addActionListener(this);
	add(animate);

	resetAlgor = new Button("reset algorithm");
	resetAlgor.setActionCommand(resetAlgor.getLabel());
	resetAlgor.addActionListener(this);
	add(resetAlgor);
	
	algor = null;
    }
    
    public synchronized void actionPerformed(ActionEvent e) 
    {
	if(e.getActionCommand().equals(reset.getLabel()))
	    resetEverything();
	if(e.getActionCommand().equals(polyMode.getLabel())) {
	    drawArea.setPolygonMode(!drawArea.polygonMode);
	    drawArea.setCloseMode(false);
	    drawArea.setPolygonNodes(drawArea.getNodes());
	    drawArea.setAnimateMode(false);
	    drawArea.repaint();
	}
	if(e.getActionCommand().equals(close.getLabel())) {
	    drawArea.setPolygonMode(true);
	    drawArea.setCloseMode(true);
	    drawArea.setPolygonNodes(drawArea.getNodes());
	    drawArea.setAnimateMode(false);
	    drawArea.repaint();
	}
	if(e.getActionCommand().equals(exec.getLabel())) {
	    polyMode.setEnabled(false);
	    close.setEnabled(false);
	    drawArea.setEnabled(false);
	    algorMode.setEnabled(false);
	    drawArea.setPolygonMode(false);
	    drawArea.setCloseMode(false);
	    drawArea.repaint();
	    if(algor != null) {
		algor.executeAlgor();
		drawArea.setAnimateMode(algor.getAnimateMode());
		drawArea.setAlgorMode(algor.getAlgorMode(AndrewConvexHull.CURRENT));
	    }
	}
	if(e.getActionCommand().equals(suspend.getLabel())) {
	    if(algor != null) {
		algor.suspendAlgor();
		drawArea.setAnimateMode(algor.getAnimateMode());
		drawArea.setAlgorMode(algor.getAlgorMode(AndrewConvexHull.CURRENT));
	    }
	}
	if(e.getActionCommand().equals(resetAlgor.getLabel())) {
	    if(algor != null) {
		algor.resetAlgor();
		drawArea.setAnimateMode(algor.getAnimateMode());
		drawArea.setAlgorMode(algor.getAlgorMode(AndrewConvexHull.CURRENT));
	    }
	    drawArea.setPolygonNodes(null);
	    drawArea.setPolygonMode(false);
	    drawArea.setCloseMode(false);
	    drawArea.setEnabled(true);
	    algorMode.setEnabled(true);
	    
	    drawArea.repaint();
	    polyMode.setEnabled(true);
	    close.setEnabled(true);
	}
	if(e.getActionCommand().equals(animate.getLabel())) {
	    if(algor != null) {
		algor.setAnimateMode(!algor.getAnimateMode());
		drawArea.setAnimateMode(algor.getAnimateMode());
		drawArea.setAlgorMode(algor.getAlgorMode(AndrewConvexHull.CURRENT));
	    }
	}
	if(e.getActionCommand().equals(algorMode.getLabel())) {
	    if(algor != null) {
		int a = algor.getAlgorMode(AndrewConvexHull.TOGGLE);
		algor.setAlgorMode(a);
		drawArea.setAlgorMode(a);
	    }
	}
    }

    public synchronized void resetEverything()
    {
	polyMode.setEnabled(true);
	close.setEnabled(true);
	algorMode.setEnabled(true);
	drawArea.initialize();
	drawArea.repaint();
	if(algor != null) {
	    algor.resetAlgor();
	    drawArea.setAnimateMode(algor.getAnimateMode());
	    drawArea.setAlgorMode(algor.getAlgorMode(AndrewConvexHull.CURRENT));
	}
    }
	
    // fixme: should grey out the polygonMode button and close button
    //        when algorithm is executing

    void setAlgorithm(Algorithm a)
    {
	algor = a;
	algor.addAlgorithmStatusListener(this);
	drawArea.setAnimateMode(algor.getAnimateMode());
    }

    public void algorithmSuspended() { return; }

    public void algorithmExecuting() 
    {
	drawArea.setEnabled(false);
	polyMode.setEnabled(false);
	close.setEnabled(false);
	algorMode.setEnabled(false);
    }
	
    public void algorithmStopped()
    {
	drawArea.setEnabled(true);
	polyMode.setEnabled(true);
	close.setEnabled(true);
	algorMode.setEnabled(true);
    }
}


