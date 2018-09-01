package crawler.planner;

import crawler.core.IWorkerMonitor;
import java.net.URL;

public abstract interface IPlanner
  extends IWorkerMonitor
{
  public abstract URL getUrl()
    throws InterruptedException;
  
  public abstract boolean checkPolicy(URL paramURL);
  
  public abstract void removeInProgress(URL paramURL);
  
  public abstract void setInProgress(URL paramURL);
  
  public abstract void registerFail(URL paramURL);
  
  public abstract void registerSuccess(URL paramURL);
  
  public abstract boolean hostEnabled(String paramString);
  
  public abstract boolean isVisitAllowed(URL paramURL);
}


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\planner\IPlanner.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */