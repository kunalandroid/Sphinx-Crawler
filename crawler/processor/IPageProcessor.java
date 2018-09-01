package crawler.processor;

import crawler.modules.std.HostsConfig;
import crawler.planner.IPlanner;
import crawler.web.Page;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;

public abstract interface IPageProcessor
{
  public abstract void init(HostsConfig paramHostsConfig, IPlanner paramIPlanner)
    throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException;
  
  public abstract void processUrls(Page paramPage);
  
  public abstract void processContent(int paramInt, Page paramPage);
  
  public abstract boolean isConnectionSuitable(HttpURLConnection paramHttpURLConnection);
  
  public abstract boolean checkRobots(URL paramURL);
  
  public abstract void logVisit(int paramInt, Page paramPage);
  
  public abstract void logVisit(URL paramURL, Page paramPage);
  
  public abstract void logVisit(int paramInt, URL paramURL);
  
  public abstract void logVisit(URL paramURL);
  
  public abstract boolean isUrlSuitable(URL paramURL);
  
  public abstract void addUrl(URL paramURL);
  
  public abstract void deleteUrl(URL paramURL);
}


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\processor\IPageProcessor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */