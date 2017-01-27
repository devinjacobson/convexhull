interface Algorithm
{
    public void setAlgorMode(int a);
    public int getAlgorMode(int i);
    public void setAnimateMode(boolean a);
    public boolean getAnimateMode();
    public void executeAlgor();
    public void suspendAlgor();
    public void resetAlgor();
    public void addAlgorithmStatusListener(AlgorithmStatusListener l);
}
