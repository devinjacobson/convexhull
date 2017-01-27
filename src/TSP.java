import java.util.*;
import java.awt.*;
/*
 * convex hull and one point we can prove
 * convex hull and two points?  
 * two convex hulls?  
 * have to check the outer hull again.
 * each time after insertion.  
 * If there are others that have to go into the outer hull, then 
 * we need to include them
 * keep the ones we have included.  
 * so if insertion < 1, then include them if they are outer hull
 * basically if there is anything within the triangle which we 
 * just excluded we have to add it too.
 * if there are more than one, then we would have to check out 
 * what occurs exactly.  
 * but for the meantime just do one.  
 * 
 *   
 * we should check all of the hulls, not just the outer.  
 * take the highest decrease from all hulls.  
 * 
 *     
 * 
 */
class TSP extends Thread 
    implements Algorithm
{
    public static final int CURRENT   = 0;
    public static final int TOGGLE    = 1;

    public static final int LINEAR = 0;
    public static final int ANDREW = 1;

    public static final int ENDMODE = ANDREW;
    
    private Vector listeners;
    private DrawingArea drawArea;

    public Nodes mySolution = null;
    public int badSolutions = 0;
    public int numreverted = 0;
    public int[] currentSolution = new int[100];
    public int[][] nodepaths = new int[10][100];
    public Nodes[] hulls = new Nodes[10];
    public Nodes[] temphulls = new Nodes[10];
    public Nodes resetNodes = new Nodes();
    public int hullcount = 0;
    public boolean[] visited = new boolean[100];
    public  boolean animateOn;
    private boolean suspended;
    private boolean stopped;

    private final int EXECUTING = 0;
    private final int STOPPED   = 1;
    private final int SUSPENDED = 2;

    int numinsertions = 0;
    private int state;
    private int mode;

    /* states for Andrew's Convex Hull algorithm */

    public TSP(DrawingArea d)
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
		    "TSP::setAlgorMode() "+a);
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
		    drawArea.initialize();
		    //drawArea.nodes.removeAllElements();
		    wait(100);
		}
	    }
	} catch (InterruptedException e) {
	    Util.dbgPrintln("TSP::resetAlgor() interruped");
	}
    }

    // check if last 3 nodes (r, p, q) makes a right turn
    private boolean rightTurn(Nodes n)
    {
	Util.Assert(n.size() >= 3, "TSP::rightTurn() "+n);
	Node r = (Node) n.elementAt(n.size()-3);
	Node p = (Node) n.elementAt(n.size()-2);
	Node q = (Node) n.elementAt(n.size()-1);

	int det = (q.x-p.x)*(r.y-p.y)-(q.y-p.y)*(r.x-p.x);
	return det < 0;
    }

    public double distance(){
    	double dist = 0;
    	for (int i=0; i<10; i++){
    		if (hulls[i] !=null){
    			dist += distance(hulls[i]);
    		}
    	}
    	return dist;
    }
    
    public double angle(Nodes n){
    	double ang = 0;
		for(int i = 2; i < n.size(); i++) {
			Node a = (Node)n.elementAt(i);
			Node b = (Node)n.elementAt(i-1);
			Node c = (Node)n.elementAt(i-2);
			double temp = b.getAngle(a, c);
			ang += Math.abs(Math.PI-Math.abs(temp));			
		}
		return ang;    	
    }
    
    public double distance(Nodes n){
    	double dist = 0;
		for(int i = 1; i < n.size(); i++) {
			Node a = (Node)n.elementAt(i);
			Node b = (Node)n.elementAt(i-1);
			double temp = distance(a, b);
			dist += temp;
			
		}
    	
    	return dist;
    	
    }
    
    public double distance(Node a, Node b){
    	return Math.sqrt((a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y));
    }

    public double perpDistance(Nodes n, Nodes m){
    	//find the closest perpendicular distance with an intersection on the 
    	//line that exists.  
    	//use that in the calculation
    	//should be able to ignore the inserted m, because the distance will be 0.
    	double total = 0;
    	for (int i=0; i<m.size(); i++){
    		double shortest = 9999999;
    		for (int j=1; j<n.size(); j++){
    			double temp = ((Node)m.elementAt(i)).getPerpendicularDistance(((Node)n.elementAt(j)), ((Node)n.elementAt(j-1)));
    			if (temp < shortest) shortest = temp;
    		}
    		if (shortest == 9999999){
    			int k=0;
    		    k+=1;
    		}
    		else{
        		total += shortest;    			
    		}
    	}
    	return total;
    	
    }


    public void fillHulls(Nodes remaining, int temphullcount){
		while (remaining.size() > 0){
			Nodes hull = new Nodes();
			//draw the inner convex hulls
			//continue until remaining == 0
			//then find insertion using the algorithm
			remaining = findHull(remaining, hull);
			hulls[temphullcount++] = hull;  			
			
		}    	
    }

	public boolean isInsideTriangle(int x1,int y1,int x2,int y2,int x3,int y3,int x4,int y4)
    {
    Polygon p=new Polygon(new int[]{x1,x2,x3},new int[]{y1,y2,y3},3);
    if(p.contains(x4,y4)) return true; 
    return false;
    }
    
	public boolean isInsideTriangle(Node A, Node B, Node C, Node n){
		return isInsideTriangle(A.x, A.y, B.x, B.y, C.x, C.y, n.x, n.y);
	}

	public Node findShorter(Node A, Node B, Node C){

		Nodes all = new Nodes();
    	copyNodes(drawArea.getNodes().sort(), all);
    	
    	//reset all to no insertionpoint if found.  
    	//reset 
    	Node n;
		for (int i=0; i<all.size(); i++){
			n = (Node)all.elementAt(i);
			if (n.insertion > 0){
				//check insertion cost against AB and BC
				if (n !=A && n !=B && n !=C){
					double insertiondist = distance(A,n)+distance(n,B)-distance(A,B);
					if (insertiondist < n.insertioncost-0.0000000005){
						//double errors or equidistant
						//found
						//if not loopback.
						int j = hulls[0].indexOf(B);
						int k = hulls[0].indexOf(n);
						while (((Node)hulls[0].elementAt(j)).insertion > 0){
							j++;
						}
						while (((Node)hulls[0].elementAt(k)).insertion > 0){
							k++;
						}
						if (k !=j){	
							//if k==j, this will be taken care of by the insertion
							//on the internal triangle.  
							return n;
						}
					}
					insertiondist =distance(B,n)+distance(n,C)-distance(B,C); 
					if (insertiondist < n.insertioncost-0.0000000005){
						//double errors or equidistant.  
						//found
						int j = hulls[0].indexOf(B);
						int k = hulls[0].indexOf(n);
						while (((Node)hulls[0].elementAt(j)).insertion > 0){
							j++;
						}
						while (((Node)hulls[0].elementAt(k)).insertion > 0){
							k++;
						}
						if (k !=j){	
							//if k==j, this will be taken care of by the insertion
							//on the internal triangle.  
							return n;
						}
					}
				}
				
			}
		}
    	return null;
    	//go through all the nodes and if there is a shorter path
	}

	public void removefromHull(Node n){
		hulls[0].remove(n);
		n.insertion = 0;
		n.insertioncost = 0;		
	}
	
	public int resetHull(Node n){
		int i = hulls[0].indexOf(n);
		int j = i;
		int k = i;
		Node p;
		while (j < hulls[0].size() && ((Node)hulls[0].elementAt(j)).insertion > 0)
			j++;
		while (k > -1 && ((Node)hulls[0].elementAt(k)).insertion > 0)
			k--;
		
		k++;
		i = 0;
		j = j-k;
		while (i++ < j){
			p = (Node)hulls[0].elementAt(k);
			removefromHull(p);
		}		
		
		return j;
	}
	public void insertShortest(Nodes hull, Node p, Node start, Node end){
		//dont actually need to check this.  
		//Just go through the whole hull is fine.  
		//it will end up being one of these almost always I would think.  
		
		int i=0;
		//end = (Node)hull.lastElement();
		for (i=0; i<hull.size() && hull.elementAt(i) != start; i++)
			;
		
		//so we are at the start
		int j=i;
		for (j=i; j<hull.size() && hull.elementAt(j) != end; j++){
			//calc dist for each
		}
		
		int insertat = 0;
		double mindist = 1000000000;
		double dist = 0;
		Node A;
		Node B;
		for (int n=i; n<j; n++){
			dist = 0;
			for (int m=i; m<j; m++){
				A = (Node)(hull.elementAt(m));
				B = (Node)(hull.elementAt(m+1));
				if (m==n){
					dist += distance(A, p) + distance(p, B);
				}
				else{
					dist += distance(A, B);
				}
					
				
			}
			if (dist < mindist){
				insertat = n;
				mindist = dist;
			}
		}
		hull.insertElementAt(p, insertat+1);
	}
	
	
    public void findInsertion(){
    	//save hulls as they are now.
    	
    	for (int i=0; i<10; i++){
    		temphulls[i] = hulls[i];
    		
    	}

    	double tempdist = 0;
    	double bestdist = 1000000000;
    	double tempang = 0;
    	int besthull1 = 0;
    	int besthull2 = 0;
    	int bestinsertion1 = 0;
    	int bestinsertion2 = 0;
    	int currenthull = 0;
    	animateOn = false;
    	//this doesnt work easily if we go through all hulls.  
    	//becasue we cant remember what we have changed.  
    	//So it only works from out to in or in to out.  
    	//it must have angle to distance factor to it.  
    	//this doesnt work with concave shapes just like 
    	//before.  
    	//shallow angle and weighted by distance.  
    	//absolute 180-angle
    	//with only two left, we have to check more.  
    	//we can easily take the wrong one.  
    	/*
    	 * both angle out in this scenario.  
			so we have to do extra searches for this I think.  
			anything that creates a < 90 degree angle needs further investigateion.  
			that subset may jump to the other side.  
so this is simply need to check the other side of the insertion.  
if we are closer to the opposite side, perhaps we need further checks.  
look how close we are to the other points on the other sides of the peaks.  
if the sum of the insertion costs are < the sum of the same insertion from the other side.  
we need to back out.  
I think we can go in order of insertion.  So after the last insertion
for the current side, we should check the insertion cost in order.  
how would we get the reorder though?  
Oh we should start from the lowest insertion on the other side.  
Then work our way backward.  
Then we get the insertion order we want.  
So we just go until the totalinsertion is > the current 
insertion cost + other insertion cost.
in the end, we need to check all of them.  
So if the total insertion cost of including another side subset 
or full set is less than the current state, then we move to that 
state, and start from the farthest point insertion.  
So we just find the minimum from an insertion cost perspective.  
is doing this at the end sufficient though?  
it is like the backout part of what we have already, but 
looking at more than a single point.  
can look at any subset.  
go from order of insertion up to X points or up to 0-0 connection.  
we have total insertion cost of one side.  
once we go above that from any opposite side, it doesnt work.  
we can go from min->max
did finish, but we are still a ways away from best.  
2610 compared to 2468 = 6% off still.  
This is huge.  
should draw the held-karp. 

  
			

    	 */
    	
    	//change this to check the internal hulls as well.  
    	//I think this is needed.  
    	//right now we are just checking the outermost hull for the next 
    	//insertion point, but I dont think this is needed or efficient.  
    	//we would have to fill the hulls internal to this, but not the ones
    	//external.  This would undo everything internal, but it would be redone.  
    	//not efficient, but perhaps effective.  
    	//can we just remove from the hull?  If it is > 3 points, I would say yes.  
    	//we shouldnt really have any overlap.  
    	while (currenthull < 1 && hulls[currenthull+1] !=null){
        	Nodes all = new Nodes();
        	copyNodes(drawArea.getNodes().sort(), all);
        	for (int f=0; f < currenthull; f++){
        		all = getRemainingNodes(hulls[f], all);
        	}
    		for (int i=0; i<hulls[currenthull+1].size(); i++){
	        	Node n = (Node)hulls[currenthull+1].get(i);
		    	for (int j=0; j<hulls[currenthull].size(); j++){
		    		hulls[currenthull].insertElementAt(n, j+1);

		    		Nodes remaining = getRemainingNodes(hulls[currenthull], all);
		    		//if remaining is outside any line in the hull, this should be nullified.
		    		//I think we have to go back farther.  
		    		//cant include anything that becomes closer than an inner hull
		    		//not sure how to calculate this best.
		    		fillHulls(remaining, currenthull+1);
		    		tempdist = distance();
		    		tempang = angle(hulls[currenthull]);
		    		if (tempdist < bestdist){
		    			bestdist = tempdist;
		    			besthull1 = currenthull;
		    			besthull2 = currenthull+1;
		    			bestinsertion1 = j;
		    			bestinsertion2 = i;
		    		}
		    		hulls[currenthull].removeElement(n);
		    	}
	    		Nodes remaining = getRemainingNodes(hulls[currenthull], all);
	    		fillHulls(remaining, currenthull+1);
    		}
    		currenthull++;
    	}
		Node n = (Node)hulls[besthull2].elementAt(bestinsertion2);
		n.insertion = ++numinsertions; 
		hulls[besthull1].insertElementAt(n, bestinsertion1+1);
		hulls[besthull2].remove(n);
		
		//check if we should include any other points.
		Nodes inside = new Nodes();
		
    	Nodes all = new Nodes();
    	copyNodes(drawArea.getNodes().sort(), all);
		Nodes remaining = getRemainingNodes(hulls[besthull1], all);
		int nhull = besthull1;
		int npoint = bestinsertion1+1;
		Node B = (Node)(hulls[nhull].elementAt(npoint));
		Node A = (Node)(hulls[nhull].elementAt(npoint-1));
		Node C = (Node)(hulls[nhull].elementAt(0));
		if (npoint+1 < hulls[nhull].size()){
			C = (Node)(hulls[nhull].elementAt(npoint+1));
		}
		B.insertioncost = distance(A, B) + distance(B, C) - distance(A, C);
		for (int i=0; i<remaining.size(); i++){
			if (isInsideTriangle(A, B, C, (Node)(remaining.elementAt(i)))){
				inside.add(remaining.elementAt(i));
			}
		}
		

		//ok so this worked for one.  
		//does it work for more?  
		//ok, it does, but we have a loop here.  
		//how can we have a loop with the same one?  
		
		if (resetNodes.size() > 10){
			int p=0;
			p+=1;
		}
		while ((n = findShorter(A, B, C)) !=null){
			int resetCount = resetHull(n);
			int hullCount = hulls[0].size();
		    Util.dbgPrintln("TSP::resetHull reset " + Integer.toString(resetCount) + " hullcount " + Integer.toString(hullCount));
		    numreverted += resetCount;
		    resetNodes.add(n);
		}
    	
    	//ok this looks better.  This insertShortest
		//is still primitive
		//but it may be sufficient.
		//check the insertioncost of previous ones and 
		//go through to see if we should use the new points.  
		//If the displacement is less for a point after any future
		//point is added, then we have to void all points 
		//included after that point.
		
		//maybe if this is longer total distance
		//we have to keep track of the expansions.  
		//and if there is one less afterwards, 
		//we have to revert.  
		
		for (int i=0; i<inside.size(); i++){
			//find best place to add AB or BC
			Node p = (Node)(inside.elementAt(i));
			//remove from current hull
			for (int k=0;k<10; k++){
				//find the hull
				if (hulls[k] !=null){
					for (int l=0; l<hulls[k].size(); l++){
						if (hulls[k].elementAt(l)==p){
							hulls[k].removeElementAt(l);
						}
					}
				}
			}
			insertShortest(hulls[nhull], p, A, C);
			
			
		}
		
    	all = new Nodes();
    	copyNodes(drawArea.getNodes().sort(), all);
		remaining = getRemainingNodes(hulls[0], all);
		fillHulls(remaining, 1);
		
    	animateOn = true;
    	//regenerate the local hulls, calculate the distance
    	//whatever is shortest we keep.  
    	//insert into the hulls[0] or whatever.  
    	//search the second convex hull for the best 
    	//insertion point into the outer hull.  
    	//the one that reduces the total hull perimeter
    	//of ALL hulls
    	//that is the point we want.  
    	//for right now anyway
    	//it is hard to say if this is optimal, but seems 
    	//at least close to optimal.  
    	//we already know the outer side that this goes to
    	//i believe.  
    	//but we dont know which point to take.  
    	//so use the same functions to create the hulls 
    	//for each temporary point insertion of the points
    	//on hulls[1] into hulls[0].  
    	//then calculate the circumferences.  
    }
    
    public void updateHulls(){
    	//search for any hull
    	for (int i=0; i<10; i++){
    		drawArea.hulls[i] = hulls[i];
    		if (hulls[i] !=null && hulls[i].size() > 0)
        	animate(hulls[i], null);
    	}
    }
    
    public void findInsertion(Nodes n, Nodes m){
    	double[][] distances = new double[n.size()][m.size()];
    	double[][] angles = new double[n.size()][m.size()];
    	
    	//should really find the remaining which is the closest to the edge first.  
    	//but this is good enough for demonstration purposes.
    	double mindistance = 100000;
    	double[] perpdistances = new double[n.size()];
    	perpdistances[0] = perpDistance(n, m);
    	for (int i=1; i<n.size(); i++){
    		for (int j=0; j<m.size(); j++){
    			n.insertElementAt(m.elementAt(j), i);
    			distances[i][j] = distance(n);
    			//add the perpendicular distances here?  
//    			distances[i][j] += perpDistance(n, m);
    			if (distances[i][j] < mindistance) mindistance = distances[i][j];
    			n.removeElementAt(i);
    		}
    	}
    	for (int i=1; i<n.size(); i++){
    		for (int j=0; j<m.size(); j++){
    			if (distances[i][j]==mindistance){
    				mindistance = 9999999;
    				for (int k=1; k<n.size(); k++){
    					n.insertElementAt(m.elementAt(j), k);
    					distances[k][j] += perpDistance(n, m);
    					if (distances[k][j] < mindistance) mindistance = distances[k][j];
    					n.removeElementAt(k);
    				}
    				for (int k=1; k<n.size(); k++){
    					if (distances[k][j]==mindistance){
    		    			n.insertElementAt(m.elementAt(j), k);
    		    			m.removeElementAt(j);
    		    			j=m.size();
    		    			i=n.size();
    		    			k=n.size();
    						
    					}
    				}
    			}
    		}
    	}
    }

    public boolean checkSolution(double distance){
    	double mydist = distance(mySolution) - 0.00000000005; //fix for Double errors
    	if (mydist > distance){
    		return true;
    	}
    	else{
    		return false;
    	}
    }
    

    public double checkKarpHeld(){
    	Karpheld k = new Karpheld();
    	
		        k.n = drawArea.getNodes().size();
		        k.cost = new double[k.n][k.n];
		        
		        k.x = new double[k.n];
		        k.y = new double[k.n];
		        for (int i = 0; i < k.n; i++) {
		        	Node n =(Node)drawArea.getNodes().elementAt(i); 
		          k.x[i] = n.getX();
		          k.y[i] = n.getY();
		        }
		        // TSPLIB distances are rounded to the nearest integer to avoid the sum of square roots problem
		        for (int i = 0; i < k.n; i++) {
		          for (int j = 0; j < k.n; j++) {
		            double dx = k.x[i] - k.x[j];
		            double dy = k.y[i] - k.y[j];
		            k.cost[i][j] = Math.rint(Math.sqrt(dx * dx + dy * dy));
		          }
		        }
		  double dkh = k.solve();
			Util.dbgPrintln("Karp-Held: " + Double.toString(k.bestNode.lowerBound));
			return dkh;
      }
    	
    public void findRoute(int start, int current, double distance, int numVisited){
    
    	int j;
    	currentSolution[numVisited] = current;
    	if (current==start){
    		if (numVisited==mySolution.size()-1){
    			if (badSolutions==0 && checkSolution(distance)){
    				badSolutions++;
    				Nodes tempnodes = new Nodes();
    				tempnodes.add(mySolution.elementAt(start));
    				for (int k=0; k< mySolution.size(); k++){
    					tempnodes.add(mySolution.elementAt(currentSolution[k]));
    				}
    				Util.dbgPrintln("better solution: "+tempnodes.toString2());
    				animate(tempnodes);
    				Util.dbgPrintln("distance: "+Double.toString(distance));
    				Util.dbgPrintln("mysolution: "+Double.toString(distance(mySolution)));
    				animateOn = false;
    				stopped = true;
    			}
    		}
    	}
		else{
			visited[current] = true;
			for (j=0; j<mySolution.size(); j++){
				if (!visited[j]){
					findRoute(start, j, distance+this.distance((Node)mySolution.elementAt(current), (Node)mySolution.elementAt(j)), numVisited+1);
				}
			}
			visited[current] = false;
		}
    	
    }
    
    
    public Nodes getRemainingNodes(Nodes n, Nodes input){
    	Nodes all = input.sort();
    	int j=0;
    	int i=0;
    	int[] keep = new int[all.size()];
    	
    	for (i=0; i< all.size(); i++){
    		for (j=0; j<n.size(); j++){
        		Node a = (Node)n.elementAt(j);
        		Node b = (Node)all.elementAt(i);
        		if (a.x==b.x && a.y==b.y){
        			keep[i] = 0;
        			j=n.size();
        		}
        		else{
        			keep[i] = 1;
        		}
    		
    		}
    	}
    	
    	/*
    	while (i<all.size() && j< n.size()){
    		Node a = (Node)n.elementAt(j);
    		Node b = (Node)all.elementAt(i);
    		if (a.x < b.x || (a.x==b.x && a.y<b.y)){
    			j++;
    		}
    		else if (a.x==b.x && a.y==b.y){
    			keep[i] = 0;
    			i++;
    			j++;
    			
    		}
    		else{
    			keep[i] = 1;
        		i++;
    		}
    	}
    	*/
    	
    	Nodes remaining = new Nodes();
    	for (i=0; i<all.size(); i++){
    		if (keep[i] > 0){
    			remaining.add(all.elementAt(i));
    		}
    	}
    	return remaining;
    
    }
    public Nodes getRemainingNodes(Nodes n){
//    	n = n.sort();
    	Nodes all = drawArea.getNodes().sort();
    	int j=0;
    	int i=0;
    	int[] keep = new int[all.size()];
    	
    	for (i=0; i< all.size(); i++){
    		for (j=0; j<n.size(); j++){
        		Node a = (Node)n.elementAt(j);
        		Node b = (Node)all.elementAt(i);
        		if (a.x==b.x && a.y==b.y){
        			keep[i] = 0;
        			j=n.size();
        		}
        		else{
        			keep[i] = 1;
        		}
    		
    		}
    	}
    	
    	/*
    	while (i<all.size() && j< n.size()){
    		Node a = (Node)n.elementAt(j);
    		Node b = (Node)all.elementAt(i);
    		if (a.x < b.x || (a.x==b.x && a.y<b.y)){
    			j++;
    		}
    		else if (a.x==b.x && a.y==b.y){
    			keep[i] = 0;
    			i++;
    			j++;
    			
    		}
    		else{
    			keep[i] = 1;
        		i++;
    		}
    	}
    	*/
    	
    	Nodes remaining = new Nodes();
    	for (i=0; i<all.size(); i++){
    		if (keep[i] > 0){
    			remaining.add(all.elementAt(i));
    		}
    	}
    	return remaining;
    }

    public void copyNodes(Nodes a, Nodes b){
		for(int i = 0; i < a.size(); i++)
		    b.addElement(a.elementAt(i));
    	
    }
    
    public void setHullCount(Nodes a){
		for(int i = 0; i < a.size(); i++)
		    ((Node)(a.elementAt(i))).hullnum = hullcount;
    	
    }
    public Nodes findHull(Nodes input, Nodes hull){
    
		Nodes upper = new Nodes();
		upper.addElement(input.elementAt(0));

		if(input.size() < 3) {
			if (input.size() > 1){
				upper.addElement(input.elementAt(1));				
			}
		    drawArea.setPolygonMode(true);
		    drawArea.setCloseMode(true);
		    drawArea.setPolygonNodes(upper);
		    stopped = true; //algorithm finished
			copyNodes(upper, hull);
			Nodes remaining = getRemainingNodes(upper, input);
			return remaining;
			
		}
		upper.addElement(input.elementAt(1));				
		
		
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
		}

		Util.dbgPrintln("TSP::run() computing lower hull");


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
		Util.Assert(lower.size() >= 2, "TSP::run() "
			    +lower.size());
		// return
		lower.removeElementAt(0);
		//lower.removeElementAt(lower.size()-1);
		copyNodes(lower, upper);
		//for(int i = 0; i < lower.size(); i++)
		//    upper.addElement(lower.elementAt(i));

		//now we have the upper which is the outer polygon.  
		copyNodes(upper, hull);
		Nodes remaining = getRemainingNodes(upper, input);
		Util.dbgPrintln("done with lower hull");
		Util.dbgPrintln("upper: "+upper.toString2());
		Util.dbgPrintln("remaining: "+remaining.toString2());
		return remaining;
		
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

		long millis = System.currentTimeMillis();
		Nodes hull = new Nodes();
		Nodes remaining = findHull(input, hull);
		Nodes orighull = new Nodes();
		drawArea.hulls[hullcount] = hull;
		hulls[hullcount++] = hull;
//		copyNodes(hull, drawArea.hulls[hullcount]);
//		copyNodes(hull, hulls[hullcount++]);
		while (remaining.size() > 0){
			hull = new Nodes();
			//draw the inner convex hulls
			//continue until remaining == 0
			//then find insertion using the algorithm
			remaining = findHull(remaining, hull);
			drawArea.hulls[hullcount] = hull;
			setHullCount(hull);
			hulls[hullcount++] = hull;
			copyNodes(hull, orighull);
//			copyNodes(hull, drawArea.hulls[hullcount]);
//			copyNodes(hull, hulls[hullcount++]);
			drawArea.hullcount = hullcount;
			
			
			//findInsertion(upper, remaining);
			//animate(upper, upper.size());
			//Util.dbgPrintln("upper: "+upper.toString2());
			//Util.dbgPrintln("remaining: "+remaining.toString2());
		}
		
		while (hulls[1] !=null && hulls[1].size() > 0){
			//find best point to enter
			findInsertion();
			//as long as there is still a second hull with 
			//some points we continue
			updateHulls();
			drawArea.distanceString="distance: "+Double.toString(distance(hulls[0])) + "\ttotal: " + Double.toString(distance());
			
		}
		

		//how long to run, very basically compare to 
		millis -= System.currentTimeMillis();
		Util.dbgPrintln("TSP::run() completed in " + Long.toString(millis));
		
		drawArea.distanceString += "\truntime " + Long.toString(millis);

		millis = System.currentTimeMillis();
		boolean check = true;
		check = false;
		if (check==true){
			mySolution = hulls[0];
			badSolutions = 0;
			Node start = (Node)mySolution.elementAt(0);
			double mindist = 100000;
			Nodes solution = new Nodes();
			for (int i=0; i< orighull.size(); i++) solution.add(orighull.elementAt(i));
			
			for (int i=1; i< mySolution.size(); i++){
				Node temp = (Node)mySolution.elementAt(i);
				findRoute(0, i, distance(start, temp), 0);
			}
		}

		//ok formatting not so nice, but we can check if it is the same or not.  
		
		double dkh = checkKarpHeld();
		
		mySolution = hulls[0];
		double d = distance(mySolution);
		Util.dbgPrintln("mysolution: "+Double.toString(d));
		if (d-5 > dkh){
			Util.dbgPrintln("UGGGHHHH");
			drawArea.distanceString += "\tKarp-Held" + Double.toString(dkh);
			badSolutions++;
		}
		Util.dbgPrintln("mysolution: \r\n"+mySolution.toString3());
		
		
		drawArea.distanceString += "\tbadsolutions" + Integer.toString(badSolutions);
		Util.dbgPrintln("TSP::run() completed");
		Util.dbgPrintln("TSP::run() badsolutions" + Integer.toString(badSolutions));
		Util.dbgPrintln("mysolution: "+Double.toString(distance(mySolution)));
		
		//how long to check
		millis -= System.currentTimeMillis();
		Util.dbgPrintln("TSP::check() completed in " + Long.toString(millis));
		drawArea.distanceString += "\tchecktime " + Long.toString(millis);
		
		drawArea.setPolygonMode(true);
		drawArea.setCloseMode(false);		
//		drawArea.setPolygonNodes(upper);

		updateHulls();
		stopped = true;
	    } catch (InterruptedException e) {
		Util.dbgPrintln("TSP::run() interruped");
	    }
	}
    }

    private final long DELAY = 50;

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

	//Util.Assert(n.size() >= 3, "TSP::animate() "+n);

	// animate the right turn by connecting the last node and
	// the 3rd last node
	//n.addElement(n.elementAt(n.size()-3));
	//drawArea.setPolygonNodes(n, 1);
	drawArea.setPolygonNodes(n, highlight);
	Util.dbgPrintln("animate: "+n.toString2());
	Util.dbgPrintln("distance: "+Double.toString(distance(n)));
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
	if (m !=null){
		for(int i = 1; i < m.size(); i++)
		    temp.addElement(m.elementAt(i));
	}	
	//remove dups before sort
    for (int i=0; i< temp.size(); i++){
    	Node a = (Node)temp.elementAt(i);
    	for (int j=0; j<temp.size(); j++){
    		if (i !=j){
    			Node b = (Node)temp.elementAt(j);
    			if (a.x==b.x && a.y==b.y){
    				temp.removeElementAt(j);
    			}
    		}
    	}
    }
    	
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
		Util.Assert(false, "TSP::throwEvent() "+type);
	    }
	}
    }
}
