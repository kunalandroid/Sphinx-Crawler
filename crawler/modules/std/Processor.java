/*     */ package crawler.modules.std;
/*     */ 
/*     */ import crawler.db.Db;
/*     */ import crawler.db.IStorage;
/*     */ import crawler.log.ConsoleHandler;
/*     */ import crawler.planner.IPlanner;
/*     */ import crawler.processor.IPageProcessor;
/*     */ import crawler.web.Page;
/*     */ import crawler.web.page.Body;
/*     */ import crawler.web.page.LinkCollection;
/*     */ import java.io.BufferedOutputStream;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.File;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.net.HttpURLConnection;
/*     */ import java.net.URL;
/*     */ import java.sql.SQLException;
/*     */ import java.util.logging.Handler;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ import utils.Compressor;
/*     */ 
/*     */ public class Processor implements IPageProcessor
/*     */ {
/*     */   protected HostsConfig config;
/*     */   protected IStorage db;
/*  30 */   protected Logger logger = Logger.getLogger(IPageProcessor.class.getName() + hashCode());
/*     */   protected IPlanner planner;
/*     */   
/*     */   public void init(HostsConfig config, IPlanner planner) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException
/*     */   {
/*  35 */     this.config = config;
/*  36 */     this.db = Db.getInstance();
/*  37 */     this.planner = planner;
/*  38 */     setLogLevel();
/*     */   }
/*     */   
/*     */   public void setLogLevel() {
/*  42 */     this.logger.setLevel(this.config.logLevel);
/*  43 */     this.logger.addHandler(new ConsoleHandler());
/*  44 */     this.logger.setUseParentHandlers(false);
/*  45 */     for (Handler handler : this.logger.getHandlers()) {
/*  46 */       if ((handler instanceof ConsoleHandler)) {
/*  47 */         handler.setLevel(this.config.logLevel);
/*     */       }
/*     */       else {
/*  50 */         this.logger.removeHandler(handler);
/*     */       }
/*     */     }
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */   public void processUrls(Page page)
/*     */   {
/*  60 */     if (!metaRobotsDenied(page.getBody().toString(), "nofollow")) {
/*  61 */       for (URL newLink : page.getLinks().values()) {
/*  62 */         addUrl(newLink);
/*     */       }
/*     */     } else {
/*  65 */       this.logger.log(Level.FINEST, "Nofollow: {0}", page.url);
/*     */     }
/*     */   }
/*     */   
/*     */   public static String getFullPath(HostsConfig config, Page page) {
/*  70 */     return config.getDirectory() + page.getFilePath();
/*     */   }
/*     */   
/*     */   public static String getFullFile(String fullPath, Page page) {
/*  74 */     return fullPath + page.getFileName();
/*     */   }
/*     */   
/*     */   public void processContent(int responseCode, Page page)
/*     */   {
/*  79 */     this.logger.log(Level.FINEST, "Processing content: {0}", page.getUrl());
/*     */     
/*  81 */     if (metaRobotsDenied(page.getBody().toString(), "noindex")) {
/*  82 */       logVisit(responseCode, page);
/*  83 */       this.logger.log(Level.FINEST, "Page indexing disabled in meta tag: {0}", page.url);
/*  84 */       return;
/*     */     }
/*     */     
/*  87 */     this.logger.log(Level.FINEST, "Checking if duplicate: {0}", page.getUrl());
/*  88 */     long start = System.currentTimeMillis();
/*  89 */     if (this.db.countByHash(page.getContentHash()) > 0)
/*     */     {
/*  91 */       logVisit(responseCode, page);
/*  92 */       this.logger.log(Level.FINEST, "Duplicate page: {0}", page.url);
/*  93 */       return;
/*     */     }
/*  95 */     this.logger.log(Level.FINEST, "Duplicate checking took {0} for {1}", new Object[] { Long.valueOf(System.currentTimeMillis() - start), page.getUrl() });
/*     */     
/*  97 */     processUrls(page);
/*     */     
/*     */ 
/* 100 */     String fullDir = getFullPath(this.config, page);
/* 101 */     new File(fullDir).mkdirs();
/*     */     
/* 103 */     String fullFilename = getFullFile(fullDir, page);
/* 104 */     BufferedWriter writer = null;
/*     */     try
/*     */     {
/* 107 */       String metaData = "<!-- URL: " + page.url + " -->\n" + "<!-- CHARSET: utf-8 -->\n";
/* 108 */       String data = metaData + page.body.toString();
/* 109 */       File file = new File(fullFilename);
/* 110 */       if (file.exists()) {
/* 111 */         this.logger.log(Level.FINEST, "Replacing {0} for {1}", new Object[] { fullFilename, page.getUrl() });
/*     */       } else {
/* 113 */         this.logger.log(Level.FINEST, "Storing {0} for {1}", new Object[] { fullFilename, page.getUrl() });
/*     */       }
/* 115 */       BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fullFilename));
/* 116 */       bos.write(this.config.compressContent ? Compressor.compress(data) : data.getBytes("UTF-8"));
/* 117 */       bos.flush();
/* 118 */       bos.close(); return;
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 122 */       this.logger.log(Level.INFO, "Unable to store file: {0}", e.getMessage());
/*     */     }
/*     */     finally
/*     */     {
/* 126 */       onContentProcessed(page);
/*     */       try
/*     */       {
/* 129 */         if (writer != null) {
/* 130 */           writer.close();
/*     */         }
/*     */       }
/*     */       catch (IOException e) {}
/*     */     }
/*     */   }
/*     */   
/*     */   public boolean metaRobotsDenied(String content, String meta)
/*     */   {
/* 139 */     Pattern p = Pattern.compile("<meta\\s+?name=(\"|')robots\\1\\s+?content=(\"|')(?<metaContent>.*?)\\2\\s*[/]?>", 2);
/* 140 */     Matcher m = p.matcher(content);
/*     */     
/* 142 */     if (!m.find()) {
/* 143 */       return false;
/*     */     }
/* 145 */     Pattern p2 = Pattern.compile(Pattern.quote(meta), 2);
/* 146 */     Matcher m2 = p2.matcher(m.group("metaContent"));
/*     */     
/* 148 */     return m2.find();
/*     */   }
/*     */   
/*     */   public boolean isUrlSuitable(URL url)
/*     */   {
/* 153 */     Pattern p = Pattern.compile("\\.(?<extension>jpg|jpgpg|jpeg|gif|png|torrent|zip|rar|pdf|doc|djvu|mp3|wav|exe|iso|gz|tar|db|xls|mobi|epub|flac|ape|cue|wma|wmv|m4a|chm|ogg|jar|bmp|sid)[\\s\n\r]?$", 2);
/* 154 */     Matcher m = p.matcher(url.getPath());
/*     */     
/* 156 */     return (!m.find()) && ((url.getProtocol().equals("http")) || (url.getProtocol().equals("https")));
/*     */   }
/*     */   
/*     */   public boolean isConnectionSuitable(HttpURLConnection connection)
/*     */   {
/* 161 */     boolean cType = connection.getContentType() != null ? Pattern.matches(".*((text/(?:html|plain))|(application/xhtml\\+xml)).*", connection.getContentType()) : false;
/* 162 */     boolean maxSizeCheck = connection.getContentLengthLong() <= this.config.maxSize;
/* 163 */     boolean rangeCheck = true;
/* 164 */     if (connection.getHeaderField("Content-Range") != null) {
/* 165 */       Pattern p = Pattern.compile("/(?<contentLength>\\d)$");
/* 166 */       Matcher m = p.matcher(connection.getHeaderField("Content-Range"));
/* 167 */       if (m.find()) {
/* 168 */         Integer contentLength = new Integer(m.group("getHeaderField"));
/* 169 */         rangeCheck = contentLength.intValue() <= this.config.maxSize;
/*     */       }
/*     */     }
/*     */     
/* 173 */     return (cType) && (maxSizeCheck);
/*     */   }
/*     */   
/*     */   public boolean checkRobots(URL url)
/*     */   {
/* 178 */     for (String mask : this.config.getDisallowedUrls(url.getHost())) {
/* 179 */       if ((mask.indexOf("*") == -1) && (url.getFile().indexOf(mask) != -1)) {
/* 180 */         return false;
/*     */       }
/*     */       
/* 183 */       mask = mask.replace("*", ".*");
/* 184 */       if (!mask.endsWith(".*")) {
/* 185 */         mask = mask + ".*";
/*     */       }
/* 187 */       mask = "^" + mask + "$";
/* 188 */       if (url.getFile().matches(mask)) {
/* 189 */         return false;
/*     */       }
/*     */     }
/*     */     
/*     */ 
/* 194 */     return true;
/*     */   }
/*     */   
/*     */   public void logVisit(int responseCode, Page page)
/*     */   {
/* 199 */     logVisit(responseCode, page.getUrl(), page.getContentHash());
/*     */   }
/*     */   
/*     */   public void logVisit(URL url, Page page)
/*     */   {
/* 204 */     logVisit(0, url, page.getContentHash());
/*     */   }
/*     */   
/*     */   public void logVisit(int responseCode, URL url)
/*     */   {
/* 209 */     logVisit(responseCode, url, null);
/*     */   }
/*     */   
/*     */   public void logVisit(URL url)
/*     */   {
/* 214 */     logVisit(0, url, null);
/*     */   }
/*     */   
/*     */   protected void logVisit(int responseCode, URL url, String contentHash) {
/* 218 */     onLogVisit(responseCode, url, contentHash);
/*     */   }
/*     */   
/*     */   public void onLogVisit(int responseCode, URL url, String contentHash) {
/* 222 */     this.db.logVisit(responseCode, url, contentHash);
/*     */   }
/*     */   
/*     */   public void addUrl(URL newLink)
/*     */   {
/* 227 */     if ((this.planner.checkPolicy(newLink)) && (isUrlSuitable(newLink))) {
/* 228 */       this.db.addUrl(newLink);
/*     */     } else {
/* 230 */       this.logger.log(Level.FINEST, "Link filtered off: {0}", newLink);
/*     */     }
/*     */   }
/*     */   
/*     */ 
/*     */   public void replaceUrl(URL url, URL replace) {}
/*     */   
/*     */   public void deleteUrl(URL url)
/*     */   {
/*     */     try
/*     */     {
/* 241 */       this.db.deleteUrl(url);
/*     */     } catch (SQLException ex) {
/* 243 */       this.logger.log(Level.SEVERE, ex.getMessage());
/*     */     }
/*     */   }
/*     */   
/*     */   protected void onContentProcessed(Page page) {}
/*     */ }


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\modules\std\Processor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */