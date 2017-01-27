import java.util.*;
import java.awt.*;

/************************************************
*    Nodes class. Contains the input points.    *
*	- draw():	draws the input points. *
************************************************/

class Nodes extends Vector
{
    public final int TOLERANCE = DrawingArea.nodeSizeDiv2 + 1;
    public static Random rand  = new Random();

    public void draw(Graphics g, Color clr) 
    {
	if(size() <= 0)
	    return;
	g.setColor(clr);
	for (Enumeration em = elements(); em.hasMoreElements();) {
	    Point pt = (Point) em.nextElement();
	    g.fillOval(pt.x - DrawingArea.nodeSizeDiv2, 
		       pt.y - DrawingArea.nodeSizeDiv2, 
		       DrawingArea.nodeSize, 
		       DrawingArea.nodeSize);
	}
    }

    public Node matchCoordToNode(int x, int y)
    {
	return matchCoordToNode(x,y,TOLERANCE);
    }

    public Node matchCoordToNode(int x, int y, int tolerance)
    {
	for(int i = 0; i < size(); i++) {
	    Node n = (Node) elementAt(i);
	    if(n.clickedOn(x,y,tolerance))
		return n;
	}
	return null;
    }

    private static int random(int range)
    {
	return Math.round( rand.nextFloat() * (float) range );
    }

    private static void swap(Vector v, int a, int b)
    {
	Object temp = v.elementAt(a);
	v.setElementAt(v.elementAt(b), a);
	v.setElementAt(temp, b);
    }

    private static int partition(Vector v, int l, int r)
    {
	int pivot, pivoty, pivotpos;
	
	pivotpos = l+random(r-l);
	swap(v, l, pivotpos);
	pivotpos = l;
	pivot  = ((Node)v.elementAt(pivotpos)).x;
	pivoty = ((Node)v.elementAt(pivotpos)).y;
	
	for (int i = l+1; i <= r; i++) {
	    Node temp = (Node) v.elementAt(i);
	    if (temp.x < pivot || (temp.x == pivot && temp.y < pivoty)) {
		pivotpos++;
		swap(v,pivotpos,i);
	    }
	}
	
	swap(v,l,pivotpos);
	return pivotpos;
    }

    private static void qSort(Vector v, int l,int r, int threshold)
    {
	int pivot;
	
	if (r-l>threshold) {
	    pivot = partition(v,l,r);
	    qSort(v,l,pivot,threshold);
	    qSort(v,pivot,r,threshold);
	}
    }

    public Nodes sort()
    {
	Nodes result = new Nodes();
	for(int i = 0; i < size(); i++)
	    result.addElement(elementAt(i));
	qSort(result, 0, result.size()-1, 1);
	return result;
    }

    // had to make this "toString2()" because jdk1.1 had this as a
    // final method in the Vector class.  How stupid!
    
    public String toString3(){
    	String s = "";    	
    	for(int i = 0; i < size(); i++){
    		Node n =((Node)elementAt(i)); 
    		s += n.toString();
    		s += " " + n.insertion;
    		s += " " + n.insertioncost;
    		s+= "\r\n";
    	}

    	return s;
    }
    
    public String toString2()
    {
	String s = "[ ";
	double angles = 0;
	for(int i = 0; i < size(); i++){
//	    s += "("+((Node) elementAt(i)).x+","+((Node) elementAt(i)).y+")";
		s += "("+((Node)elementAt(i)).insertion + ")";
	    if (i>0 && i< size()-1){
	    	double angle = ((Node)elementAt(i)).getAngle(((Node)elementAt(i-1)), ((Node)elementAt(i+1)));
	    	if (!(new Double(angle).isNaN())){
	    		angles += angle;
	    	}
	    	s += " angle: " + Double.toString(angle);
	    }
	    s+= "\r\n";
	}
	s+= Double.toString(angles);
	s += " ]";
	return s;
    }
}
