package crawler.db;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public abstract interface IStorage
{
  public abstract void addUrl(URL paramURL);
  
  public abstract void logVisit(int paramInt, URL paramURL, String paramString);
  
  public abstract int countByHash(String paramString);
  
  public abstract String[] preloadQueue(String paramString, int paramInt1, int paramInt2, ArrayList<String> paramArrayList);
  
  public abstract void reset(String paramString);
  
  public abstract void truncate()
    throws SQLException;
  
  public abstract void deleteUrl(URL paramURL)
    throws SQLException;
  
  public abstract ResultSet getDataByUrl(URL paramURL);
  
  public abstract Statement createStatement()
    throws SQLException;
  
  public abstract void resetById(long paramLong);
}


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\db\IStorage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */