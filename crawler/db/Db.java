/*    */ package crawler.db;
/*    */ 
/*    */ import crawler.core.Config;
/*    */ import java.sql.SQLException;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public final class Db
/*    */ {
/*    */   protected static volatile IStorage instance;
/*    */   
/*    */   public static IStorage getInstance()
/*    */     throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException
/*    */   {
/* 17 */     IStorage localInstance = instance;
/* 18 */     if (localInstance == null) {
/* 19 */       synchronized (Class.forName(Config.storageClass)) {
/* 20 */         localInstance = instance;
/* 21 */         if (localInstance == null) {
/* 22 */           instance = localInstance = (IStorage)Class.forName(Config.storageClass).newInstance();
/*    */         }
/*    */       }
/*    */     }
/* 26 */     return localInstance;
/*    */   }
/*    */ }


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\db\Db.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */