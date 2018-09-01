/*     */ package crawler.modules.std;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.InputStreamReader;
/*     */ import java.net.Proxy;
/*     */ import java.net.URL;
/*     */ import java.net.URLConnection;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class RobotsParser
/*     */ {
/*     */   protected HostsConfig config;
/*  21 */   protected ConcurrentHashMap<URL, String> contentContainer = new ConcurrentHashMap();
/*     */   
/*     */   public RobotsParser(HostsConfig config) {
/*  24 */     this.config = config;
/*     */   }
/*     */   
/*     */   public void run() throws InterruptedException {
/*  28 */     RobotsFetcher[] threads = new RobotsFetcher[this.config.initialUrl.size()];
/*  29 */     int i = 0;
/*  30 */     for (URL initialUrl : this.config.initialUrl) {
/*  31 */       RobotsFetcher f = new RobotsFetcher(initialUrl, this, this.config.proxy);
/*  32 */       f.start();
/*  33 */       threads[(i++)] = f;
/*     */     }
/*     */     int idle;
/*     */     do
/*     */     {
/*  38 */       idle = 0;
/*  39 */       for (i = 0; i < threads.length; i++) {
/*  40 */         if (!threads[i].isAlive()) {
/*  41 */           idle++;
/*     */         }
/*     */       }
/*  44 */       Thread.sleep(100L);
/*  45 */     } while (idle < threads.length);
/*     */     
/*  47 */     for (URL initialUrl : this.config.initialUrl) {
/*  48 */       parse(initialUrl, (String)this.contentContainer.get(initialUrl));
/*     */     }
/*     */   }
/*     */   
/*     */   public void addContent(URL initialUrl, String content) {
/*  53 */     this.contentContainer.put(initialUrl, content);
/*     */   }
/*     */   
/*     */   public void parse(URL initialUrl, String content) {
/*  57 */     Pattern uaPattern = Pattern.compile("^User-agent:\\s*(?<ua>.+)[\r]?", 2);
/*  58 */     Pattern disallowPattern = Pattern.compile("^Disallow:\\s*(?<disallow>.+)[\r]?", 2);
/*  59 */     String[] lines = content.split("\n");
/*  60 */     String currentUserAgent = "";
/*     */     
/*  62 */     for (String line : lines) {
/*  63 */       Matcher uaMatcher = uaPattern.matcher(line);
/*  64 */       if (uaMatcher.find()) {
/*  65 */         currentUserAgent = uaMatcher.group("ua");
/*     */       }
/*     */       else
/*     */       {
/*  69 */         Matcher disMatcher = disallowPattern.matcher(line);
/*  70 */         if ((this.config.robotsUa.contains(currentUserAgent)) && (disMatcher.find())) {
/*  71 */           String disallowedUrl = disMatcher.group("disallow");
/*     */           
/*     */ 
/*  74 */           if (!disallowedUrl.equals("/"))
/*  75 */             ((ArrayList)this.config.disallowedUrls.get(initialUrl.getHost())).add(disallowedUrl);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */   
/*     */   protected static class RobotsFetcher extends Thread {
/*     */     protected URL initialUrl;
/*     */     protected RobotsParser parser;
/*     */     protected Proxy proxy;
/*     */     
/*  86 */     public RobotsFetcher(URL initialUrl, RobotsParser parser, Proxy proxy) { setName("RobotsFetcher for " + initialUrl.getHost());
/*  87 */       this.initialUrl = initialUrl;
/*  88 */       this.parser = parser;
/*  89 */       this.proxy = proxy;
/*     */     }
/*     */     
/*     */     public void run()
/*     */     {
/*  94 */       String content = fetchRobots(this.initialUrl);
/*  95 */       this.parser.addContent(this.initialUrl, content);
/*     */     }
/*     */     
/*     */     protected String fetchRobots(URL initialUrl) {
/*     */       try {
/* 100 */         URL url = new URL(initialUrl, "/robots.txt");
/* 101 */         URLConnection yc = this.proxy != null ? url.openConnection(this.proxy) : url.openConnection();
/* 102 */         yc.connect();
/*     */         
/*     */ 
/* 105 */         BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
/*     */         
/*     */ 
/*     */ 
/*     */ 
/* 110 */         StringBuilder content = new StringBuilder();
/* 111 */         String inputLine; while ((inputLine = in.readLine()) != null)
/* 112 */           content.append(inputLine).append("\n");
/* 113 */         return content.toString();
/*     */       }
/*     */       catch (RuntimeException e) {
/* 116 */         throw e;
/*     */       }
/*     */       catch (Exception e) {}
/* 119 */       return "";
/*     */     }
/*     */   }
/*     */ }


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\modules\std\RobotsParser.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */