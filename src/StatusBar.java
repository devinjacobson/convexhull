import java.awt.*;

class StatusBar extends Panel implements AlgorithmStatusListener
{
    Label editMode, polygonMode, closeMode, algorithmState, animateMode,
	algorMode;

    public StatusBar(int x, int y)
    {
	super();
	preferredSize = new Dimension(x, y);
	setLayout(new FlowLayout(FlowLayout.CENTER, 5, 1));

	editMode      = new Label();
	polygonMode   = new Label();
	closeMode     = new Label();
	algorithmState = new Label();
	animateMode   = new Label();
	algorMode     = new Label();
	
	setEditMode(true);
	setPolygonMode(false);
	setCloseMode(false);
	setAnimateMode(false);
	setAlgorMode(AndrewConvexHull.ANDREW);
	algorithmStopped();

	add(editMode);
	add(polygonMode);
	add(closeMode);
	add(algorMode);
	add(algorithmState);
	add(animateMode);

	validate();
    }

    Dimension preferredSize;
    public Dimension getPreferredSize() 
    {
	return preferredSize;
    }

    public void setPolygonMode(boolean polygon)
    {
	String s = "PolygonMode: ";
	if(polygon)
	    s = s+"ON";
	else
	    s = s+"OFF";
	polygonMode.setText(s);
    }

    public void setCloseMode(boolean closed)
    {
	String s = "ClosedMode: ";
	if(closed)
	    s = s+"ON";
	else
	    s = s+"OFF";
	closeMode.setText(s);
    }

    private final String algorTitle = "ExecStatus: ";

    public void setEditMode(boolean edit)
    {
	String s = "Edit Mode: ";
	if(edit)
	    s = s+"ON";
	else
	    s = s+"OFF";
	editMode.setText(s);
    }

    public void setAnimateMode(boolean a)
    {
	String s = "Animate: ";
	if(a)
	    s = s+"ON";
	else
	    s = s+"OFF";
	animateMode.setText(s);
    }

    public void setAlgorMode(int a)
    {
	// should fix this static varible to a proper function call
	Util.Assert(a >= 0 && a <= AndrewConvexHull.ENDMODE, "AndrewConvexHull::setAlgorMode() "+a);
	String s = "AlgorMode: ";
	if(a == AndrewConvexHull.LINEAR)
	    s += "LINEAR";
	else
	    s += "ANDREW";
	algorMode.setText(s);
    }

    public void algorithmSuspended()
    {
	algorithmState.setText(algorTitle+"SUSPENDED");
	doLayout();
    }

    public void algorithmExecuting()
    {
	algorithmState.setText(algorTitle+"EXECUTING");
	doLayout();
    }

    public void algorithmStopped()
    {
	algorithmState.setText(algorTitle+"STOPPED");
	doLayout();
    }

}
