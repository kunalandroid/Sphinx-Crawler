/*    */ package utils;
/*    */ 
/*    */ import crawler.modules.std.Worker;
/*    */ import java.security.MessageDigest;
/*    */ import java.security.NoSuchAlgorithmException;
/*    */ import java.util.logging.Level;
/*    */ import java.util.logging.Logger;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class Md5
/*    */ {
/*    */   public static String getHash(String str)
/*    */   {
/*    */     try
/*    */     {
/* 19 */       MessageDigest md = MessageDigest.getInstance("MD5");
/* 20 */       md.update(str.getBytes());
/* 21 */       byte[] digest = md.digest();
/*    */       
/* 23 */       StringBuffer hexString = new StringBuffer();
/*    */       
/* 25 */       for (int i = 0; i < digest.length; i++) {
/* 26 */         String s = Integer.toHexString(0xFF & digest[i]);
/* 27 */         hexString.append(s.length() == 1 ? "0" + s : s);
/*    */       }
/*    */       
/* 30 */       return hexString.toString();
/*    */     }
/*    */     catch (NoSuchAlgorithmException ex) {
/* 33 */       Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
/*    */     }
/*    */     
/* 36 */     return null;
/*    */   }
/*    */ }


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\utils\Md5.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */