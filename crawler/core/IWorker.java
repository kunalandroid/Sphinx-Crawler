package crawler.core;

import crawler.modules.std.HostsConfig;
import crawler.planner.IPlanner;

public abstract interface IWorker
  extends Runnable
{
  public abstract void init(IPlanner paramIPlanner, HostsConfig paramHostsConfig)
    throws Exception;
  
  public abstract boolean isIdle();
  
  public abstract IWorkerMonitor getMonitor();
  
  public abstract void setName(String paramString);
  
  public abstract String getName();
}


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\core\IWorker.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */