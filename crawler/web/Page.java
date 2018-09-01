/*     */ package crawler.web;
/*     */ 
/*     */ import crawler.web.page.Body;
/*     */ import crawler.web.page.LinkCollection;
/*     */ import java.io.File;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.URL;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.SQLException;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ import utils.Md5;
/*     */ 
/*     */ public class Page
/*     */ {
/*     */   public Body body;
/*     */   public URL url;
/*     */   public String encoding;
/*     */   protected LinkCollection links;
/*  20 */   protected String file = "";
/*     */   
/*     */   public Page(URL url, String content) {
/*  23 */     this.url = url;
/*  24 */     this.body = new Body(content);
/*  25 */     this.links = extractLinks();
/*     */   }
/*     */   
/*     */   public LinkCollection getLinks()
/*     */   {
/*  30 */     return this.links;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   protected final LinkCollection extractLinks()
/*     */   {
/*  39 */     Object[] linkStrings = utils.Parser.getLinks(this.body.toString());
/*  40 */     LinkCollection urls = new LinkCollection();
/*  41 */     String baseHref = getBaseHref();
/*  42 */     for (Object link : linkStrings)
/*     */     {
/*     */ 
/*  45 */       if (isLinkRelative((String)link)) {
/*  46 */         link = baseHref + link;
/*     */       }
/*     */       
/*     */       try
/*     */       {
/*  51 */         urls.put((String)link, createUrl((String)link));
/*     */       }
/*     */       catch (MalformedURLException e) {}
/*     */     }
/*     */     
/*     */ 
/*     */ 
/*  58 */     return urls;
/*     */   }
/*     */   
/*     */   protected URL createUrl(String link) throws MalformedURLException
/*     */   {
/*  63 */     return URLManager.createUrl(this.url, link);
/*     */   }
/*     */   
/*     */   public String getUrlHash()
/*     */   {
/*  68 */     return Md5.getHash(this.url.toString());
/*     */   }
/*     */   
/*     */   public String getContent() {
/*  72 */     return getBody().toString();
/*     */   }
/*     */   
/*     */   public void setContent(String content) {
/*  76 */     getBody().setContent(content);
/*     */   }
/*     */   
/*     */   public String getContentHash() {
/*  80 */     return Md5.getHash(this.body.toString());
/*     */   }
/*     */   
/*     */   public String getTitle() {
/*  84 */     Pattern p = Pattern.compile("<title>(?<title>.*?)</title>", 2);
/*  85 */     Matcher m = p.matcher(getBody().toString());
/*  86 */     if (m.find()) {
/*  87 */       return m.group("title");
/*     */     }
/*     */     
/*  90 */     return "";
/*     */   }
/*     */   
/*     */   public Body getBody()
/*     */   {
/*  95 */     return this.body;
/*     */   }
/*     */   
/*     */   public URL getUrl() {
/*  99 */     return this.url;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */   public String getFilePath()
/*     */   {
/* 107 */     return getUrl().getHost() + File.separator + getUrlHash().substring(0, 1) + File.separator + getUrlHash().substring(1, 2) + File.separator;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public String getFileName()
/*     */   {
/* 117 */     return getUrlHash() + ".html";
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */   public String getFile()
/*     */   {
/* 125 */     return getFilePath() + getFileName();
/*     */   }
/*     */   
/*     */   public static Page fromResultSet(ResultSet rs) throws MalformedURLException, SQLException {
/* 129 */     return new Page(new URL(rs.getString("url")), "");
/*     */   }
/*     */   
/*     */   public String getBaseHref() {
/* 133 */     Pattern p = Pattern.compile("<base\\shref=\"(?<baseHref>.*)\"", 2);
/* 134 */     Matcher m = p.matcher(getContent());
/* 135 */     String baseHref = "";
/* 136 */     if (m.find()) {
/* 137 */       baseHref = m.group("baseHref");
/* 138 */       if (!baseHref.endsWith("/")) {
/* 139 */         baseHref = baseHref + "/";
/*     */       }
/*     */     }
/*     */     
/* 143 */     return baseHref;
/*     */   }
/*     */   
/*     */   private boolean isLinkRelative(String link) {
/* 147 */     Pattern p = Pattern.compile("^(https?:/)?/", 2);
/* 148 */     Matcher m = p.matcher(link);
/* 149 */     return !m.find();
/*     */   }
/*     */ }


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\web\Page.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */