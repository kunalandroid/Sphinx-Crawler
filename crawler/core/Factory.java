/*    */ package crawler.core;
/*    */ 
/*    */ public class Factory {
/*    */   public static Class getWorkerClass(String module) throws ClassNotFoundException {
/*  5 */     return getClass(module, "Worker");
/*    */   }
/*    */   
/*    */   public static Class getHostsConfigClass(String module) throws ClassNotFoundException {
/*  9 */     return getClass(module, "HostsConfig");
/*    */   }
/*    */   
/*    */   public static Class getProcessorClass(String module) throws ClassNotFoundException {
/* 13 */     return getClass(module, "Processor");
/*    */   }
/*    */   
/*    */   public static boolean moduleExists(String module) {
/* 17 */     char first = Character.toUpperCase(module.charAt(0));
/* 18 */     String className = first + module.substring(1) + "Module";
/*    */     try {
/* 20 */       Class.forName("crawler.modules." + module + "." + className);
/* 21 */       return true;
/*    */     } catch (ClassNotFoundException e) {}
/* 23 */     return false;
/*    */   }
/*    */   
/*    */   protected static Class getClass(String module, String className) throws ClassNotFoundException
/*    */   {
/*    */     try {
/* 29 */       return Class.forName("crawler.modules." + module + "." + className);
/*    */     } catch (ClassNotFoundException e) {}
/* 31 */     return Class.forName("crawler.modules.std." + className);
/*    */   }
/*    */ }


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\core\Factory.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */