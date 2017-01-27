import java.awt.*;

class Node extends Point 
{
	double tempangle;
	double tempdistance;
	double insertioncost;
	public int insertion;
	public int hullnum;
	public static double distance(Node a, Node b){
    	return Math.sqrt((a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y));
	}
	
	public static double distance(Node a, double x, double y){
		return Math.sqrt((a.x-x)*(a.x-x)+(a.y-y)*(a.y-y));
	}
	
	public double getAngle(Node a, Node b){
		//get the angle for a->this->b
		double p12 = Math.pow(distance(this,a),2);
		double p13 = Math.pow(distance(this,b),2);
		double temp = Math.pow(distance(a,b),2)-p12-p13;
		double angle = Math.acos(temp/(2*Math.sqrt(p12)*Math.sqrt(p13)));
		return angle;
		
	}
	
	public double getPerpendicularDistance(Node a, Node b){
		//get teh perpendicular distance from this point to line a->b
		double slope1 = 0;
		double slope2 = 0;
		if (b.y==a.y){
			slope1 = 99999999;
			slope2 = 0;
		}
		else{
			slope1 = ((double)(b.y-a.y))/(b.x-a.x);
			slope2 = -1/slope1;
		}
		double b1 = (double)a.y-slope1*a.x;
		double b2 =(double)this.y-slope2*this.x;
		double intersectx = (b2-b1)/(slope1-slope2);
		double intersecty = slope1*intersectx+b1;
		
		double tempdist;
		int minx = a.x;
		int maxx = b.x;
		if (b.x < a.x){
			minx = b.x;
			maxx = a.x;
		}
		if (intersectx < minx || intersectx > maxx){
			tempdist = distance(this, a);
			double temp2 = distance(this, b);
			if (temp2 < tempdist) tempdist = temp2;
			return tempdist;
		}
		else{
			return distance(this, intersectx, intersecty);
		}		
		//we really want to multiply this by the angle to some extent.  
		//whatever the actual distance is through this point
		//but thenagain we are just extending teh search one out.  
		//this improves somewhat.  
	}
	
    public Node(int x, int y)
    {
	super(x, y);
	insertion = 0;
	insertioncost = 0;
    }
    
    public boolean clickedOn(int x, int y, int tolerance)
    {
	return 
	    Math.abs(this.x - x) < tolerance &&
	    Math.abs(this.y - y) < tolerance;
    }

    public Object clone()
    {
	return new Node(x, y);
    }
}
