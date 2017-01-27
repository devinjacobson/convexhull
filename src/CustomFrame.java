import java.awt.event.*;
import java.awt.*;

/**
 * A frame for the BeaconFinderApp GUI.
 *
 * @author Allen Miu
 */
public class CustomFrame extends Frame {

    Dimension preferredSize = null;

    public CustomFrame (String frameTitle) 
    {
	super(frameTitle);
	//same thing
	//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent event) {
		    System.exit(1);
		} 
	    });
    }

    public CustomFrame (String frameTitle, int x, int y) 
    {
	this(frameTitle);
	preferredSize = new Dimension(x,y);
	setResizable(false);
	//	setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    public Dimension getPreferredSize() 
    {
	if(preferredSize != null)
	    return preferredSize;
	else 
	    return super.getPreferredSize();
    }
}

