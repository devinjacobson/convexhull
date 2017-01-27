import java.awt.*;
import java.awt.event.*;
import java.util.*;

/****************************************************************
*    Canvas class. Accpets inputs and draws the graph.		*
*	- initialize():	Initializes the whole graph;		*
*	- mouseDown():	Reacts to clicks or shift-clicks;	*
*	- mouseDrag():  Reacts to mouse drags;			*
*	- addPoint():	Add a point and update display;		*
*	- deletePoint():Delete a point and update display;	*
*	- run():	Show animation;				*
*	- paint():	Draw graphs.				*
****************************************************************/

class DrawingArea 
    extends Canvas 
    implements MouseListener, MouseMotionListener {
    public Nodes nodes; 		//Point set
    private Nodes polygonNodes;
    public Nodes[] hulls = new Nodes[10];
    public int hullcount = 0;
    public String distanceString = "";
    

    private Point prevMousePos, selectPos;
    private Image   img;
    private Graphics gc;

    public int numLinksToHighlight;

    /* these are accessed by the Controls object */
    boolean polygonMode;
    boolean closeMode;

    public static final int nodeSize = 6; // assume this to be even
    public static final int nodeSizeDiv2 = nodeSize/2;

    private StatusBar statusBar;

    public DrawingArea(int x, int y, StatusBar s) 
    {
	super();
	preferredSize = new Dimension(x, y);
	setBackground(Color.white);
	addMouseListener(this);
	addMouseMotionListener(this);
	Util.Assert(s != null, "DrawingArea()");
	statusBar = s;
	initialize();
    }

    public void setAnimateMode(boolean b)
    {
	statusBar.setAnimateMode(b);
    }

    public void setAlgorMode(int b)
    {
	statusBar.setAlgorMode(b);
    }
    
    public void setEnabled(boolean enable)
    {
	super.setEnabled(enable);
	statusBar.setEditMode(enable);
    }

    void setCloseMode(boolean mode)
    {
	closeMode = mode;
	statusBar.setCloseMode(mode);
    }

    void setPolygonMode(boolean mode)
    {
	polygonMode = mode;
	statusBar.setPolygonMode(mode);
    }

    Dimension preferredSize;
    public Dimension getPreferredSize() 
    {
	return preferredSize;
    }

    void setImageAndGC(Image i, Graphics g) 
    {
	img = i; gc = g;
    }

    // Initialize data structure
    void initialize() 
    {
	if (nodes == null)
	    nodes = new Nodes();
	else 
	    nodes.removeAllElements();
	setPolygonNodes(nodes);
	setPolygonMode(false);
	setCloseMode(false);
	setEnabled(true);
	prevMousePos = null;
    }

    public void mousePressed(MouseEvent e) {
	int x        = e.getX();
	int y        = e.getY();
	Node pt      = nodes.matchCoordToNode(x, y);
	int modifier = e.getModifiers();

	Util.dbgPrintln("mpressed: "+modifier+" "+MouseEvent.BUTTON3_MASK);
	
	if ((modifier & MouseEvent.BUTTON3_MASK) > 0) {
	    if(pt != null) {
		Util.dbgPrintln("Deleting point at (" +x+","+y+").");
		deletePoint(pt);
	    }
	    prevMousePos = selectPos = null;
	    repaint();
	    return;
	}
	else if(pt == null) {  //adding point
	    Util.dbgPrintln("Adding point at (" +x+","+y+").");
	    closeMode  = false;
	    Graphics g = getGraphics();
	    g.setColor(Color.blue);
	    g.fillOval(x - nodeSizeDiv2, y - nodeSizeDiv2, nodeSize, nodeSize);
	    
	    pt = new Node(x, y);
	    addPoint(pt);
	}

	prevMousePos = pt;
	selectPos    = (Node) pt.clone();
    }

    private boolean isVerticalDisplacement(int x, int y)
    {
	Util.Assert(selectPos != null, 
		    "DrawingArea::isVerticalDisplacement");
	double slope = (double) (x - selectPos.x)/(y - selectPos.y);
	return Math.abs(slope) >= 1.0;
    }

    public void mouseDragged(MouseEvent e) 
    {
	int x        = e.getX();
	int y        = e.getY();
	int modifier = e.getModifiers();

	if (!((modifier & MouseEvent.BUTTON1_MASK) > 0))
	    return;

	/* to restrict displacement to either HORIZONTAL or VERTICAL */
	if ((modifier & MouseEvent.SHIFT_MASK) > 0) {
	    if(isVerticalDisplacement(x, y))
		y = selectPos.y;
	    else
		x = selectPos.x;
	}

	int xx = Math.max(Math.min(x, getPreferredSize().width-nodeSizeDiv2), nodeSizeDiv2);
	int yy = Math.max(Math.min(y, getPreferredSize().height-nodeSizeDiv2), nodeSizeDiv2);

	Util.Assert(prevMousePos != null, "DrawingArea::mouseDragged()");
	Util.dbgPrintln("Dragging: (" + xx + ", " + yy + ")");
	prevMousePos.setLocation(xx, yy);
	repaint();
    }

    public void mouseClicked(MouseEvent e) { return; }
    public void mouseEntered(MouseEvent e) { return; }
    public void mouseExited(MouseEvent e) { return; }
    public void mouseReleased(MouseEvent e) { return; }
    public void mouseMoved(MouseEvent e) { return; }
    
    void addPoint(Node pt) 
    {
	nodes.addElement(pt);
	repaint();
    }

    void deletePoint(Point point) 
    {
	nodes.removeElementAt(nodes.indexOf(point));
	repaint();
    }

    public void clear()
    {
	initialize();
    }

    public void setPolygonNodes(Nodes n)
    {
	setPolygonNodes(n, 0);
    }

    public void setPolygonNodes(Nodes n, int l)
    {
	// check to make sure n contains only valid nodes, i.e.
	// nodes that are in the nodes list.
	if(n != null) {
	    for(int i = 0; i < n.size(); i++) {
		if(!nodes.contains(n.elementAt(i)))
		    Util.Assert(false, "DrawingArea::setPolygonNodes "+i);
	    }
	}

	numLinksToHighlight = l;
	polygonNodes = n;
	repaint();
    }

    public Nodes getNodes()
    {
	return nodes;
    }

    public void paint(Graphics g) 
    {
	if(img != null) {
	    //double buffering to avoid flickering
	    gc.setColor(getBackground());
	    gc.fillRect(0, 0, getPreferredSize().width, 
			getPreferredSize().height);
	    gc.setColor(getForeground());
	    nodes.draw(gc, Color.black);
	    if(polygonMode && polygonNodes != null){
	    	PolygonDrawer.draw(polygonNodes, gc, Color.red, closeMode, numLinksToHighlight);
	    }
	    for (int i=0; i<hullcount; i++){
			PolygonDrawer.draw(hulls[i], gc, Color.red, closeMode, hulls[i].size());	    
	    }
	    g.drawImage(img, 0, 0, this);
	    g.drawString(distanceString, 0, 20);
	}
    }
}

class PolygonDrawer
{
    public static void draw(Nodes n, Graphics g, Color clr, boolean closeMode, int numLinksToHighlight) {
	if(n.size() <= 0)
	    return;

	Point first = null;
	Point prev = null;
	Point pt   = null;
	
	first = prev = pt = (Point) n.elementAt(0);
	for(int i = 1; i < n.size(); i++) {
	    pt = (Node) n.elementAt(i);
	    if(i > numLinksToHighlight)
	    	g.setColor(clr);
	    else if (numLinksToHighlight == -1)
	    	g.setColor(Color.white);
	    else
	    	g.setColor(Color.blue);
	    g.drawLine(prev.x, prev.y , pt.x, pt.y);
	    g.drawString(Integer.toString(((Node)pt).insertion) + "(" + Integer.toString(((Node)pt).hullnum) + ")", pt.x, pt.y);
	    prev = pt;
	}	    
	if(closeMode && !first.equals(pt)) 
	    g.drawLine(pt.x, pt.y , first.x, first.y);
    }
}




