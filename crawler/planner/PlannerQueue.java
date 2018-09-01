/*    */ package crawler.planner;
/*    */ 
/*    */ import java.util.concurrent.LinkedBlockingQueue;
/*    */ 
/*    */ public class PlannerQueue extends LinkedBlockingQueue<String>
/*    */ {
/*  7 */   protected java.util.concurrent.ConcurrentHashMap hosts = new java.util.concurrent.ConcurrentHashMap();
/*    */   
/*    */ 
/*    */   public boolean add(String url)
/*    */   {
/* 12 */     return super.add(url);
/*    */   }
/*    */ }


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\planner\PlannerQueue.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */