package crawler.core;

public abstract interface IWorkerMonitor
{
  public abstract void setIsStop(boolean paramBoolean);
  
  public abstract boolean getIsStop();
  
  public abstract boolean isAlive();
}


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\core\IWorkerMonitor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */