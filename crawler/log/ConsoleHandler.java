/*    */ package crawler.log;
/*    */ 
/*    */ import java.io.PrintStream;
/*    */ import java.text.SimpleDateFormat;
/*    */ import java.util.Date;
/*    */ import java.util.logging.Formatter;
/*    */ import java.util.logging.Level;
/*    */ import java.util.logging.LogRecord;
/*    */ 
/*    */ public class ConsoleHandler
/*    */   extends java.util.logging.ConsoleHandler
/*    */ {
/*    */   public static class CrawlerConsoleFormatter extends Formatter
/*    */   {
/*    */     public String format(LogRecord lr)
/*    */     {
/* 17 */       SimpleDateFormat df = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
/* 18 */       StringBuilder buf = new StringBuilder(180);
/* 19 */       buf.append(df.format(new Date(lr.getMillis())));
/* 20 */       buf.append(" ");
/* 21 */       buf.append(formatMessage(lr));
/*    */       
/* 23 */       return buf.toString();
/*    */     }
/*    */   }
/*    */   
/*    */   public ConsoleHandler()
/*    */   {
/* 29 */     setFormatter(new CrawlerConsoleFormatter());
/*    */   }
/*    */   
/*    */   public void publish(LogRecord lr)
/*    */   {
/* 34 */     if (lr.getLevel().intValue() > Level.INFO.intValue()) {
/* 35 */       System.err.println(getFormatter().format(lr));
/*    */     }
/*    */     else {
/* 38 */       System.out.println(getFormatter().format(lr));
/*    */     }
/*    */   }
/*    */ }


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\log\ConsoleHandler.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */