/*    */ package crawler.modules.sphinx;
/*    */ 
/*    */ import crawler.db.IStorage;
/*    */ import crawler.planner.IPlanner;
/*    */ import crawler.web.Page;
/*    */ import java.net.URL;
/*    */ import java.sql.ResultSet;
/*    */ import java.sql.SQLException;
/*    */ import java.util.logging.Level;
/*    */ import java.util.logging.Logger;
/*    */ 
/*    */ public class Processor
/*    */   extends crawler.modules.std.Processor
/*    */ {
/*    */   protected Sphinx sphinx;
/*    */   
/*    */   public void init(crawler.modules.std.HostsConfig config, IPlanner planner)
/*    */     throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException
/*    */   {
/* 20 */     super.init(config, planner);
/* 21 */     this.sphinx = new Sphinx((HostsConfig)config);
/* 22 */     this.sphinx.init();
/*    */   }
/*    */   
/*    */   public void deleteUrl(URL url)
/*    */   {
/*    */     try {
/* 28 */       this.sphinx.deleteDocument(getUrlId(url));
/* 29 */       super.deleteUrl(url);
/*    */     } catch (SQLException ex) {
/* 31 */       Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, ex.getMessage());
/*    */     }
/*    */   }
/*    */   
/*    */   protected void onContentProcessed(Page page)
/*    */   {
/* 37 */     super.onContentProcessed(page);
/*    */     
/* 39 */     ResultSet data = getDataByUrl(page.getUrl());
/* 40 */     if (data != null) {
/*    */       try {
/* 42 */         addDocument(data, page);
/*    */       } catch (SQLException ex) {
/* 44 */         this.logger.log(Level.SEVERE, "Unable to save document to sphinx''s index (2): {0}\n{1}\nTrying to reconnect...", new Object[] { page.getUrl(), ex.getMessage() });
/*    */         try {
/* 46 */           this.sphinx.reconnect();
/* 47 */           addDocument(data, page);
/*    */         } catch (SQLException ex1) {
/* 49 */           this.logger.log(Level.SEVERE, "Save to sphinx failed: {0}\n{1}", new Object[] { page.getUrl(), ex1.getMessage() });
/*    */         }
/*    */         
/*    */       }
/*    */     } else {
/* 54 */       this.logger.log(Level.SEVERE, "Unable to get sphinx''s document (0): {0}", page.getUrl());
/*    */     }
/*    */   }
/*    */   
/*    */   protected void addDocument(ResultSet data, Page page) throws SQLException {
/* 59 */     this.logger.log(Level.FINE, "Saving data to sphinx''s index: {0}, {1}", new Object[] { Long.valueOf(data.getLong("id")), page.getUrl() });
/* 60 */     if ((this.sphinx.addDocument(this.sphinx.createDocument(data, page))) && (this.sphinx.exists(data.getLong("id")))) {
/* 61 */       this.logger.log(Level.FINE, "Sucessfully saved to sphinx: {0}, {1}", new Object[] { Long.valueOf(data.getLong("id")), page.getUrl() });
/*    */     }
/*    */     else {
/* 64 */       this.logger.log(Level.SEVERE, "Unable to save document to sphinx''s index (1): {0}", page.getUrl());
/*    */     }
/*    */   }
/*    */   
/*    */   protected int getUrlId(URL url) {
/* 69 */     ResultSet rs = getDataByUrl(url);
/* 70 */     if (rs != null) {
/*    */       try {
/* 72 */         return rs.getInt("id");
/*    */       } catch (SQLException ex) {
/* 74 */         return 0;
/*    */       }
/*    */     }
/*    */     
/* 78 */     return 0;
/*    */   }
/*    */   
/*    */   protected ResultSet getDataByUrl(URL url)
/*    */   {
/* 83 */     return this.db.getDataByUrl(url);
/*    */   }
/*    */ }


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\modules\sphinx\Processor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */