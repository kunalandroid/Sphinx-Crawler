/*    */ package utils;
/*    */ 
/*    */ import java.util.HashSet;
/*    */ import java.util.regex.Matcher;
/*    */ import java.util.regex.Pattern;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class Parser
/*    */ {
/*    */   public static Object[] getLinks(String html)
/*    */   {
/* 15 */     HashSet<String> urls = new HashSet();
/*    */     
/* 17 */     String[] patterns = { "<a(.+?)href=(['\"])(?<url>[^\"'#]+?)(?:\\2|#)[^>]*>", "<(i)?frame(.+?)src=(['\"])(?<url>[^#]+?)(?:\\3|#)" };
/*    */     
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/* 23 */     for (String pattern : patterns)
/*    */     {
/* 25 */       Pattern p = Pattern.compile(pattern, 2);
/* 26 */       Matcher m = p.matcher(html);
/* 27 */       while (m.find()) {
/* 28 */         if (!urls.contains(m.group("url"))) {
/* 29 */           urls.add(m.group("url").replace("&amp;", "&"));
/*    */         }
/*    */       }
/*    */     }
/* 33 */     return urls.toArray();
/*    */   }
/*    */ }


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\utils\Parser.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */