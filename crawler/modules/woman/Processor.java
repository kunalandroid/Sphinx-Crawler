/*    */ package crawler.modules.woman;
/*    */ 
/*    */ import crawler.modules.std.HostsConfig;
/*    */ import crawler.modules.std.Worker;
/*    */ import crawler.modules.std.Worker.CrawlerSizeExceededException;
/*    */ import crawler.planner.IPlanner;
/*    */ import crawler.web.Page;
/*    */ import crawler.web.page.Body;
/*    */ import java.io.IOException;
/*    */ import java.net.Authenticator;
/*    */ import java.net.HttpURLConnection;
/*    */ import java.net.MalformedURLException;
/*    */ import java.net.PasswordAuthentication;
/*    */ import java.net.URL;
/*    */ import java.sql.SQLException;
/*    */ import java.util.logging.Level;
/*    */ import java.util.logging.Logger;
/*    */ import java.util.regex.Matcher;
/*    */ import java.util.regex.Pattern;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class Processor
/*    */   extends crawler.modules.std.Processor
/*    */ {
/*    */   public void init(HostsConfig config, IPlanner planner)
/*    */     throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException
/*    */   {
/* 30 */     super.init(config, planner);
/* 31 */     Authenticator.setDefault(new Authenticator()
/*    */     {
/*    */       protected PasswordAuthentication getPasswordAuthentication() {
/* 34 */         return new PasswordAuthentication("dev", "m412bf".toCharArray());
/*    */       }
/*    */     });
/*    */   }
/*    */   
/*    */   public void processContent(int responseCode, Page page)
/*    */   {
/* 41 */     onContentProcessed(page);
/*    */   }
/*    */   
/*    */   protected void onContentProcessed(Page page)
/*    */   {
/* 46 */     HttpURLConnection paolaConnection = null;
/*    */     try {
/* 48 */       URL paolaUrl = new URL(new URL("http://woman.paola.woman.ru"), page.getUrl().getFile());
/* 49 */       paolaConnection = (HttpURLConnection)(this.config.proxy != null ? paolaUrl.openConnection(this.config.proxy) : paolaUrl.openConnection());
/* 50 */       String[] paolaContent = Worker.fetchContent(paolaConnection);
/* 51 */       Pattern p = Pattern.compile("<title>(?<title>.*?)</title>", 2);
/* 52 */       Matcher wMatcher = p.matcher(page.getBody().toString());
/* 53 */       Matcher pMatcher = p.matcher(paolaContent[0]);
/*    */       
/*    */ 
/* 56 */       String wTitle = wMatcher.find() ? wMatcher.group("title") : "";
/* 57 */       wTitle = wTitle.replace("  | Woman.ru", " | Woman.ru");
/* 58 */       wTitle = wTitle.replace("  | форум Woman.ru", " | форум Woman.ru");
/*    */       
/* 60 */       String pTitle = pMatcher.find() ? pMatcher.group("title") : "";
/* 61 */       pTitle = pTitle.replace("  | Woman.ru", " | Woman.ru");
/* 62 */       pTitle = pTitle.replace("  | форум Woman.ru", " | форум Woman.ru");
/*    */       
/* 64 */       Object[] params = { page.getUrl(), wTitle, paolaUrl, pTitle };
/* 65 */       if (!wTitle.equals(pTitle)) {
/* 66 */         this.logger.log(Level.FINE, "\nTitle mismatch: \n{0}: {1}\n{2}: {3}", params);
/* 67 */       } else if (wTitle.equals("")) {
/* 68 */         this.logger.log(Level.FINE, "\nEmpty title: \n{0}: {1}\n{2}: {3}", params);
/*    */       }
/*    */       
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */       try
/*    */       {
/* 84 */         if ((paolaConnection != null) && (paolaConnection.getResponseCode() == 200)) {}
/*    */         
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/* 90 */         return;ex = ex;
/*    */       }
/*    */       catch (IOException ex1)
/*    */       {
/* 88 */         Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex1);
/*    */       }
/*    */       return;
/*    */     }
/*    */     catch (MalformedURLException ex) {}catch (IOException ex)
/*    */     {
/*    */       try
/*    */       {
/* 84 */         if ((paolaConnection != null) && (paolaConnection.getResponseCode() == 200)) {}
/*    */         
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/* 90 */         return;ex = ex;
/*    */       }
/*    */       catch (IOException ex1)
/*    */       {
/* 88 */         Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex1);
/*    */       }
/*    */     }
/*    */     catch (Worker.CrawlerSizeExceededException localObject)
/*    */     {
/*    */       try
/*    */       {
/* 84 */         if ((paolaConnection != null) && (paolaConnection.getResponseCode() == 200)) {}
/*    */         
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/* 90 */         return;localObject = finally;
/*    */       }
/*    */       catch (IOException ex1)
/*    */       {
/* 88 */         Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex1);
/*    */       }
/*    */     }
/*    */     finally
/*    */     {
/*    */       try
/*    */       {
/* 84 */         if ((paolaConnection != null) && (paolaConnection.getResponseCode() == 200)) {}
/*    */         
/*    */ 
/*    */ 
/*    */ 
/* 89 */         throw ((Throwable)localObject);
/*    */       }
/*    */       catch (IOException ex1)
/*    */       {
/* 88 */         Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex1);
/*    */       }
/*    */     }
/*    */   }
/*    */ }


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\modules\woman\Processor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */