import java.util.*;
import java.awt.*;

class AndrewConvexHull extends Thread 
    implements Algorithm
{
    public static final int CURRENT   = 0;
    public static final int TOGGLE    = 1;

    public static final int LINEAR = 0;
    public static final int ANDREW = 1;

    public static final int ENDMODE = ANDREW;
    
    private Vector listeners;
    private DrawingArea drawArea;

    public  boolean animateOn;
    private boolean suspended;
    private boolean stopped;

    private final int EXECUTING = 0;
    private final int STOPPED   = 1;
    private final int SUSPENDED = 2;

    private int state;
    private int mode;

    /* states for Andrew's Convex Hull algorithm */

    public AndrewConvexHull(DrawingArea d)
    {
	drawArea  = d;	
	suspended = false;
	stopped   = true;
	animateOn = true;
	mode      = ANDREW;
	listeners = new Vector();
    }

    public void setAlgorMode(int a)
    {
	Util.Assert(a >= 0 && a <= ENDMODE, 
		    "AndrewConvexHull::setAlgorMode() "+a);
	mode = a;
    }
    
    public int getAlgorMode(int i)
    {
	int result = -1;
	if(i == CURRENT)
	    result = mode;
	else if (i == TOGGLE)
	    result = (mode+1) % (ENDMODE + 1);
	return result;
    }

    public boolean getAnimateMode()
    {
	return animateOn;
    }

    public void setAnimateMode(boolean a)
    {
	animateOn = a;
    }

    public synchronized void executeAlgor()
    {
	suspended = false;
	stopped   = false;
	notify();
    }

    public synchronized void suspendAlgor()
    {
        suspended = true;
	stopped   = false;
	notify();
    }

    public void resetAlgor()
    {
	try {
	    synchronized(this) {
		// spin wait until algorithm is suspended
		while(state != STOPPED) {
		    // reset states
		    mode      = ANDREW;
		    suspended = false;
		    stopped   = true;
		    animateOn = true;
		    interrupt();
		    wait(100);
		}
	    }
	} catch (InterruptedException e) {
	    Util.dbgPrintln("AndrewConvexHull::resetAlgor() interruped");
	}
    }

    // check if last 3 nodes (r, p, q) makes a right turn
    private boolean rightTurn(Nodes n)
    {
	Util.Assert(n.size() >= 3, "AndrewConvexHull::rightTurn() "+n);
	Node r = (Node) n.elementAt(n.size()-3);
	Node p = (Node) n.elementAt(n.size()-2);
	Node q = (Node) n.elementAt(n.size()-1);

	int det = (q.x-p.x)*(r.y-p.y)-(q.y-p.y)*(r.x-p.x);
	return det < 0;
    }

    public synchronized void run()
    {
	while(true) {
	    try {
		// this should be STOPPED instead
		while(stopped) {
		    synchronized(this) {
			throwEvent(STOPPED);
			wait();
		    }
		}
		throwEvent(EXECUTING);

		Nodes input = null;
		if(mode == LINEAR)
		    input = drawArea.getNodes().sort();
		else if(mode == ANDREW)
		    input = drawArea.getNodes().sort();

		Util.dbgPrintln("mode: "+mode+" input: "+input.toString2());

		if(input.size() == 0) {
		    stopped = true;
		    continue;
		}

		Nodes upper = new Nodes();
		upper.addElement(input.elementAt(0));

		if(input.size() < 2) {
		    drawArea.setPolygonMode(true);
		    drawArea.setCloseMode(true);
		    drawArea.setPolygonNodes(upper);
		    stopped = true; //algorithm finished
		    continue;
		}

		upper.addElement(input.elementAt(1));
		
		if(input.size() < 3) {
		    drawArea.setPolygonMode(true);
		    drawArea.setCloseMode(true);
		    drawArea.setPolygonNodes(upper);
		    stopped = true; //algorithm finished
		    continue;
		}
		
		for(int i = 2; i < input.size(); i++) {
		    upper.addElement(input.elementAt(i));
		    animate(upper);
		    while(upper.size() > 2 && rightTurn(upper)) {
			upper.removeElementAt(upper.size() - 2);
			animate(upper);
		    }
		}
		
		if(mode == LINEAR) {
		    drawArea.setPolygonMode(true);
		    drawArea.setCloseMode(true);		
		    drawArea.setPolygonNodes(upper);
		    stopped = true;
		    continue;
		}

		Util.dbgPrintln("AndrewConvexHull::run() computing lower hull");
		
		Nodes lower = new Nodes();
		lower.addElement(input.elementAt(input.size()-1));
		lower.addElement(input.elementAt(input.size()-2));
		for(int i = input.size()-3; i >= 0; i--) {
		    lower.addElement(input.elementAt(i));
		    animate(upper, lower);
		    while(lower.size() > 2 && rightTurn(lower)) {
			lower.removeElementAt(lower.size() - 2);
			animate(upper, lower);
		    }
		}
		Util.Assert(lower.size() >= 2, "AndrewConvexHull::run() "
			    +lower.size());
		// return
		lower.removeElementAt(0);
		//lower.removeElementAt(lower.size()-1);
		for(int i = 0; i < lower.size(); i++)
		    upper.addElement(lower.elementAt(i));
		drawArea.setPolygonMode(true);
		drawArea.setCloseMode(false);		
		drawArea.setPolygonNodes(upper);
		
		stopped = true;
	    } catch (InterruptedException e) {
		Util.dbgPrintln("AndrewConvexHull::run() interruped");
	    }
	}
    }

    private final long DELAY = 750;

    private void animate(Nodes n)
    {
	animate(n, 0);
    }

    private void animate(Nodes n, int highlight)
    {
	if(!animateOn)
	    return;
	if(state == STOPPED)
	    return;

      	// should draw the last three
	drawArea.setPolygonMode(true);
	drawArea.setCloseMode(false);

	//Util.Assert(n.size() >= 3, "AndrewConvexHull::animate() "+n);

	// animate the right turn by connecting the last node and
	// the 3rd last node
	//n.addElement(n.elementAt(n.size()-3));
	//drawArea.setPolygonNodes(n, 1);
	drawArea.setPolygonNodes(n, highlight);
	Util.dbgPrintln("animate: "+n.toString2());
	try {
	    synchronized(this) {
		if(suspended) {
		    throwEvent(SUSPENDED);
		    wait();
		}
		else
		    wait(DELAY);
		throwEvent(EXECUTING);
	    }
	}
	catch (InterruptedException e) {
	}
	finally {
	    //n.removeElementAt(n.size()-1); // remove the extra node
	}
    }
    
    private void animate(Nodes n, Nodes m)
    {
	if(!animateOn)
	    return;
	if(state == STOPPED)
	    return;
	
	Nodes temp = new Nodes();
	for(int i = 0; i < n.size(); i++)
	    temp.addElement(n.elementAt(i));
	for(int i = 1; i < m.size(); i++)
	    temp.addElement(m.elementAt(i));
	animate(temp, n.size()-1);
    }

    public void addAlgorithmStatusListener(AlgorithmStatusListener l)
    {
	listeners.addElement(l);
    }

    public void throwEvent(int type)
    {
	state = type;
	for(int i = 0; i < listeners.size(); i++) {
	    AlgorithmStatusListener l = 
		(AlgorithmStatusListener) listeners.elementAt(i);
	    switch(type) {
	    case EXECUTING:
		l.algorithmExecuting(); break;
	    case STOPPED:
		l.algorithmStopped(); break;
	    case SUSPENDED:
		l.algorithmSuspended(); break;
	    default:
		Util.Assert(false, "AndrewConvexHull::throwEvent() "+type);
	    }
	}
    }
}
