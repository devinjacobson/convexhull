public class Util 
{
    public static boolean debugMode = true;//false;

    public static void dbgPrintln(String s) 
    {
	if(debugMode) 
	    System.out.println(s);
    }
    
    public static void Assert(boolean pred, String errMsg) 
    {
	try {
	    if(!pred)
		throw new Exception();
	}
	catch (Exception e) {
	    System.err.println("ASSERTION FAILED--> "+errMsg);
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}
