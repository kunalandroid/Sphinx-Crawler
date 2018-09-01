/*     */ package crawler.core;
/*     */ 
/*     */ import crawler.db.Db;
/*     */ import crawler.db.IStorage;
/*     */ import crawler.log.ConsoleHandler;
/*     */ import crawler.modules.std.HostsConfig;
/*     */ import crawler.planner.PlannerPolicy;
/*     */ import crawler.planner.PlannerQueue;
/*     */ import java.io.PrintStream;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.URL;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Timestamp;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Random;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ import java.util.logging.Handler;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ 
/*     */ public class Planner extends Thread implements crawler.planner.IPlanner
/*     */ {
/*     */   protected PlannerQueue queue;
/*     */   protected HostsConfig config;
/*  26 */   protected ArrayList<String> hosts = new ArrayList();
/*  27 */   protected ArrayList<String> disabledHosts = new ArrayList();
/*  28 */   protected ArrayList<String> lastPreloaded = new ArrayList();
/*  29 */   protected PlannerPolicy policy = PlannerPolicy.HOST;
/*  30 */   protected ConcurrentHashMap<String, URL> known = new ConcurrentHashMap();
/*  31 */   protected ConcurrentHashMap<String, String> visited = new ConcurrentHashMap();
/*  32 */   protected ConcurrentHashMap<String, String> inProgress = new ConcurrentHashMap();
/*  33 */   private ConcurrentHashMap<String, Integer> failCounter = new ConcurrentHashMap();
/*  34 */   public Logger logger = Logger.getLogger(getClass().getName() + hashCode());
/*  35 */   private boolean isStop = false;
/*     */   
/*     */ 
/*     */ 
/*     */   public Planner(HostsConfig config, String name)
/*     */   {
/*  41 */     super(name);
/*  42 */     this.queue = new PlannerQueue();
/*  43 */     this.config = config;
/*     */   }
/*     */   
/*     */   public Planner(HostsConfig config) {
/*  47 */     this(config, null);
/*     */   }
/*     */   
/*     */   public void init() {
/*  51 */     setLogLevel();
/*  52 */     for (String host : this.config.getHosts()) {
/*  53 */       if (!this.hosts.contains(host)) {
/*  54 */         this.hosts.add(host);
/*  55 */         this.failCounter.put(host, Integer.valueOf(0));
/*     */       }
/*     */     }
/*  58 */     this.logger.log(Level.FINER, "[{0}] Preloading initial urls for {1}...", new Object[] { this.config.getSection(), this.config.getHosts() });
/*  59 */     preload();
/*  60 */     if (this.queue.size() > 0) {
/*  61 */       this.logger.log(Level.FINER, "[{0}] Done, {1} url(s) preloaded.", new Object[] { this.config.getSection(), Integer.valueOf(this.queue.size()) });
/*  62 */     } else if (isRevisitDisabled()) {
/*  63 */       this.logger.log(Level.FINER, "[{0}] Nothing to do. Stopping.", this.config.getSection());
/*  64 */       setIsStop(true);
/*     */     } else {
/*  66 */       this.logger.log(Level.FINER, "[{0}] Revisit pending. Waiting.", this.config.getSection());
/*     */     }
/*     */   }
/*     */   
/*     */   protected void setLogLevel() {
/*  71 */     this.logger.setLevel(this.config.logLevel);
/*  72 */     this.logger.setUseParentHandlers(false);
/*  73 */     this.logger.addHandler(new ConsoleHandler());
/*  74 */     for (Handler handler : this.logger.getHandlers()) {
/*  75 */       if ((handler instanceof ConsoleHandler)) {
/*  76 */         handler.setLevel(this.config.logLevel);
/*     */       }
/*     */       else {
/*  79 */         this.logger.removeHandler(handler);
/*     */       }
/*     */     }
/*     */   }
/*     */   
/*     */   public void run()
/*     */   {
/*  86 */     init();
/*  87 */     if (getIsStop()) {
/*     */       return;
/*     */     }
/*     */     for (;;) {
/*  91 */       preload();
/*     */       
/*  93 */       if ((this.queue.isEmpty()) && (this.inProgress.isEmpty()) && (isRevisitDisabled()))
/*     */       {
/*  95 */         setIsStop(true);
/*  96 */         synchronized (this) {
/*  97 */           notifyAll();
/*     */         }
/*  99 */         return;
/*     */       }
/* 101 */       if (this.queue.isEmpty())
/*     */       {
/*     */         try {
/* 104 */           Thread.sleep(10000L);
/*     */         } catch (InterruptedException ex) {
/* 106 */           this.logger.log(Level.SEVERE, ex.getMessage());
/* 107 */           setIsStop(true);
/* 108 */           notifyAll();
/* 109 */           return;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 114 */         synchronized (this) {
/* 115 */           notifyAll();
/*     */         }
/*     */         try {
/* 118 */           Thread.sleep(5000L);
/*     */         } catch (InterruptedException ex) {
/* 120 */           this.logger.log(Level.SEVERE, ex.getMessage());
/* 121 */           setIsStop(true);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */   
/*     */ 
/*     */   public synchronized void registerSuccess(URL url)
/*     */   {
/* 130 */     this.failCounter.put(url.getHost(), Integer.valueOf(0));
/*     */   }
/*     */   
/*     */   public synchronized void registerFail(URL url)
/*     */   {
/* 135 */     Integer fc = (Integer)this.failCounter.get(url.getHost());
/* 136 */     this.failCounter.put(url.getHost(), Integer.valueOf(fc.intValue() + 1));
/*     */     
/* 138 */     if (((Integer)this.failCounter.get(url.getHost())).intValue() > this.config.maxFailedRequests) {
/* 139 */       disableHost(url.getHost());
/*     */     }
/*     */   }
/*     */   
/*     */   private void disableHost(String host) {
/* 144 */     this.logger.log(Level.FINE, "Reached maxFailedRequests, host seems to be down, stopping crawling: {0}", host);
/* 145 */     this.disabledHosts.add(host);
/*     */   }
/*     */   
/*     */   public boolean hostEnabled(String host)
/*     */   {
/* 150 */     return !this.disabledHosts.contains(host);
/*     */   }
/*     */   
/*     */   public void setIsStop(boolean isStop)
/*     */   {
/* 155 */     this.isStop = isStop;
/*     */   }
/*     */   
/*     */   public boolean getIsStop()
/*     */   {
/* 160 */     return this.isStop;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   public URL getUrl()
/*     */     throws InterruptedException
/*     */   {
/*     */     try
/*     */     {
/* 170 */       String urlStr = (String)this.queue.poll();
/* 171 */       if (urlStr != null) {
/* 172 */         return new URL(urlStr);
/*     */       }
/*     */       
/* 175 */       return null;
/*     */     }
/*     */     catch (MalformedURLException ex) {}
/* 178 */     return getUrl();
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public boolean addUrl(String url)
/*     */   {
/* 188 */     return this.queue.add(url);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public boolean checkPolicy(URL url)
/*     */   {
/* 198 */     if (this.policy == PlannerPolicy.HOST) {
/* 199 */       return this.hosts.contains(url.getHost());
/*     */     }
/*     */     
/*     */ 
/* 203 */     return false;
/*     */   }
/*     */   
/*     */ 
/*     */   public void setInProgress(URL url)
/*     */   {
/* 209 */     this.inProgress.put(url.toString(), url.toString());
/*     */   }
/*     */   
/*     */   public void removeInProgress(URL url)
/*     */   {
/* 214 */     this.inProgress.remove(url.toString());
/*     */   }
/*     */   
/*     */   public boolean isVisitAllowed(URL url)
/*     */   {
/*     */     try {
/* 220 */       ResultSet rs = Db.getInstance().getDataByUrl(url);
/* 221 */       boolean timeCondition = true;
/* 222 */       if (rs != null) {
/* 223 */         Timestamp t = rs.getTimestamp("fetched_at");
/* 224 */         timeCondition = (t == null) || (this.config.revisitDisabled()) || (System.currentTimeMillis() - t.getTime() >= this.config.revisitAfterMillis());
/*     */       }
/* 226 */       System.out.println(url);
/* 227 */       return (!this.inProgress.contains(url.toString())) && (checkPolicy(url)) && (timeCondition);
/*     */     } catch (SQLException|ClassNotFoundException|InstantiationException|IllegalAccessException ex) {
/* 229 */       this.logger.log(Level.SEVERE, "{0}", ex); }
/* 230 */     return false;
/*     */   }
/*     */   
/*     */   private void preload()
/*     */   {
/* 235 */     if (!this.queue.isEmpty()) {
/* 236 */       return;
/*     */     }
/*     */     try {
/* 239 */       ArrayList<String> preloaded = new ArrayList();
/* 240 */       ArrayList<String> lastPreloadedStrings = new ArrayList();
/* 241 */       for (String last : this.lastPreloaded) {
/* 242 */         lastPreloadedStrings.add(last);
/*     */       }
/*     */       
/* 245 */       for (String inProgressUrl : this.inProgress.values()) {
/* 246 */         lastPreloadedStrings.add(inProgressUrl.toString());
/*     */       }
/*     */       
/* 249 */       int limit = 1500;
/* 250 */       int hostNumber = this.config.getHosts().size();
/* 251 */       int limitByHost = (int)Math.ceil(limit / hostNumber);
/*     */       
/* 253 */       for (String host : this.config.getHosts()) {
/* 254 */         if (!this.disabledHosts.contains(host)) {
/* 255 */           String[] preloadedStrings = Db.getInstance().preloadQueue(host, this.config.revisitAfter, limitByHost, lastPreloadedStrings);
/* 256 */           preloaded.addAll(java.util.Arrays.asList(preloadedStrings));
/*     */         }
/*     */       }
/*     */       
/* 260 */       if (preloaded.size() > 0) {
/* 261 */         long seed = System.nanoTime();
/* 262 */         java.util.Collections.shuffle(preloaded, new Random(seed));
/* 263 */         for (String url : preloaded) {
/* 264 */           addUrl(url);
/*     */         }
/* 266 */         this.lastPreloaded = preloaded;
/*     */       }
/*     */     } catch (SQLException|ClassNotFoundException|InstantiationException|IllegalAccessException ex) {
/* 269 */       Logger.getLogger(Planner.class.getName()).log(Level.SEVERE, null, ex);
/*     */     }
/*     */   }
/*     */   
/*     */   public boolean isRevisitDisabled() {
/* 274 */     return this.config.revisitDisabled();
/*     */   }
/*     */ }


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\core\Planner.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */