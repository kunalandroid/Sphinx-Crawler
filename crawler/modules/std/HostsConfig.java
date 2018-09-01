/*     */ package crawler.modules.std;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.net.Proxy;
/*     */ import java.net.URL;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.logging.Level;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class HostsConfig
/*     */ {
/*  18 */   public boolean enabled = true;
/*  19 */   public Level logLevel = Level.FINE;
/*  20 */   public String module = "std";
/*  21 */   public String directory = "data";
/*     */   
/*     */   private String section;
/*  24 */   public int maxSize = 10240;
/*  25 */   public int maxRate = 1;
/*  26 */   public int maxRetries = 3;
/*  27 */   public int maxFailedRequests = 10;
/*  28 */   public int maxRedirects = 3;
/*  29 */   public int revisitAfter = -1;
/*  30 */   public int workers = 10;
/*     */   
/*  32 */   public boolean reset = false;
/*  33 */   public boolean compressContent = false;
/*  34 */   public String userAgent = "Direct Bot/1.0";
/*  35 */   public ArrayList<String> robotsUa = new ArrayList();
/*  36 */   public HashMap<String, ArrayList<String>> disallowedUrls = new HashMap();
/*  37 */   public HashMap<String, ArrayList<String>> allowedUrls = new HashMap();
/*     */   public ArrayList<URL> initialUrl;
/*  39 */   public Proxy proxy = null;
/*  40 */   protected ArrayList<String> hosts = new ArrayList();
/*     */   
/*     */   public HostsConfig() {
/*  43 */     this.robotsUa.add("*");
/*  44 */     this.robotsUa.add("direct");
/*     */   }
/*     */   
/*     */   public ArrayList<String> getHosts() {
/*  48 */     if (this.hosts.isEmpty()) {
/*  49 */       initHosts();
/*     */     }
/*     */     
/*  52 */     return this.hosts;
/*     */   }
/*     */   
/*     */   public void init() {
/*  56 */     initHosts();
/*  57 */     initDirectory();
/*  58 */     this.maxSize = (this.maxSize != -1 ? this.maxSize * 1024 : this.maxSize);
/*     */   }
/*     */   
/*     */   public void initHosts() {
/*  62 */     for (URL url : this.initialUrl) {
/*  63 */       String host = url.getHost();
/*  64 */       if (!this.hosts.contains(host)) {
/*  65 */         this.hosts.add(host);
/*  66 */         this.disallowedUrls.put(host, new ArrayList());
/*     */       }
/*     */     }
/*     */   }
/*     */   
/*     */   public void initDirectory() {
/*  72 */     if (!this.directory.endsWith(File.separator))
/*  73 */       this.directory = this.directory.concat(File.separator);
/*     */   }
/*     */   
/*     */   public String getDirectory() {
/*  77 */     return this.directory;
/*     */   }
/*     */   
/*     */   public String getModule() {
/*  81 */     return this.module;
/*     */   }
/*     */   
/*     */   public Level getLogLevel() {
/*  85 */     return this.logLevel;
/*     */   }
/*     */   
/*     */   public ArrayList<String> getDisallowedUrls(String host) {
/*  89 */     return (ArrayList)this.disallowedUrls.get(host);
/*     */   }
/*     */   
/*     */   public static String getDefaultModule() {
/*  93 */     return "std";
/*     */   }
/*     */   
/*     */   public void setSection(String section) {
/*  97 */     this.section = section;
/*     */   }
/*     */   
/*     */   public String getSection() {
/* 101 */     return this.section;
/*     */   }
/*     */   
/*     */   public boolean revisitDisabled() {
/* 105 */     return this.revisitAfter == -1;
/*     */   }
/*     */   
/*     */   public long revisitAfterMillis() {
/* 109 */     return this.revisitAfter * 1000;
/*     */   }
/*     */ }


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\modules\std\HostsConfig.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */