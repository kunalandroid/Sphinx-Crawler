/*    */ package crawler.web;
/*    */ 
/*    */ import java.net.URL;
/*    */ import java.util.LinkedHashSet;
/*    */ import java.util.regex.Matcher;
/*    */ import java.util.regex.Pattern;
/*    */ 
/*    */ public class URLManager
/*    */ {
/*    */   public static URL createUrl(URL sourceUrl, String link) throws java.net.MalformedURLException
/*    */   {
/*    */     try
/*    */     {
/* 14 */       return new URL(sourceUrl, new java.net.URI(stripSids(link)).normalize().toString());
/*    */     } catch (java.net.URISyntaxException ex) {
/* 16 */       throw new java.net.MalformedURLException(ex.getMessage());
/*    */     }
/*    */   }
/*    */   
/*    */   public static String stripSids(String link) {
/* 21 */     LinkedHashSet<String> patterns = new LinkedHashSet();
/* 22 */     patterns.add("(?i)^(.+)(?:&(?:(?:jsessionid)|(?:phpsessid))=[0-9a-zA-Z]{32})(?:&(.*))?$");
/* 23 */     patterns.add("(?i)^(.+)(?:&sid=[0-9a-zA-Z]{32})(?:&(.*))?$");
/* 24 */     patterns.add("(?i)^(.+)(?:&ASPSESSIONID[a-zA-Z]{8}=[a-zA-Z]{24})(?:&(.*))?$");
/*    */     
/* 26 */     patterns.add("(?i)^(.+)(?:(?:(?:jsessionid)|(?:phpsessid))=[0-9a-zA-Z]{32})(?:&(.*))?$");
/* 27 */     patterns.add("(?i)^(.+)(?:sid=[0-9a-zA-Z]{32})(?:&(.*))?$");
/* 28 */     patterns.add("(?i)^(.+)(?:ASPSESSIONID[a-zA-Z]{8}=[a-zA-Z]{24})(?:&(.*))?$");
/*    */     
/* 30 */     for (String pStr : patterns) {
/* 31 */       Pattern p = Pattern.compile(pStr);
/* 32 */       Matcher m = p.matcher(link);
/* 33 */       link = m.matches() ? checkForNull(m.group(1)) + checkForNull(m.group(2)) : link;
/*    */     }
/*    */     
/* 36 */     return link;
/*    */   }
/*    */   
/*    */   public static String checkForNull(String string) {
/* 40 */     return string != null ? string : "";
/*    */   }
/*    */ }


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\web\URLManager.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */