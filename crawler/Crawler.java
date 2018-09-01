/*     */ package crawler;
/*     */ 
/*     */ import crawler.core.ConfigParser;
/*     */ import crawler.core.IWorker;
/*     */ import crawler.core.Planner;
/*     */ import crawler.db.Db;
/*     */ import crawler.db.IStorage;
/*     */ import crawler.log.ConsoleHandler;
/*     */ import crawler.modules.std.HostsConfig;
/*     */ import crawler.modules.std.RobotsParser;
/*     */ import java.io.File;
/*     */ import java.lang.reflect.Constructor;
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ import java.net.CookieHandler;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.URL;
/*     */ import java.sql.SQLException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.logging.Handler;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ 
/*     */ public class Crawler
/*     */ {
/*  25 */   protected static boolean stop = false;
/*  26 */   protected static final Logger logger = Logger.getLogger(Crawler.class.getName());
/*     */   
/*     */   public static void main(String[] args) throws InterruptedException, MalformedURLException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, IllegalArgumentException, InvocationTargetException, SQLException, Exception
/*     */   {
/*     */     
/*     */     try
/*     */     {
/*  33 */       ConfigParser configParser = new ConfigParser();
/*  34 */       configParser.setLogger(logger);
/*  35 */       ArrayList<HostsConfig> configs = configParser.getConfigs();
/*  36 */       logger.log(Level.INFO, "Config loaded");
/*     */       
/*  38 */       if (configs.isEmpty()) {
/*  39 */         throw new IllegalArgumentException("No initial urls found");
/*     */       }
/*     */       
/*  42 */       run(configs);
/*     */     }
/*     */     catch (ClassNotFoundException ex) {
/*  45 */       logger.log(Level.SEVERE, ex.getMessage());
/*     */     }
/*     */   }
/*     */   
/*     */   public static void run(ArrayList<HostsConfig> configs) throws InterruptedException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, IllegalArgumentException, InvocationTargetException, SQLException, Exception {
/*  50 */     stop = false;
/*  51 */     CookieHandler.setDefault(new java.net.CookieManager());
/*  52 */     boolean stopOnIdle = true;
/*  53 */     for (HostsConfig config : configs)
/*     */     {
/*  55 */       new File(config.directory).mkdirs();
/*     */       
/*  57 */       logger.log(Level.INFO, "Processing robots.txt for {0}", config.initialUrl);
/*     */       
/*     */ 
/*  60 */       RobotsParser rp = new RobotsParser(config);
/*  61 */       rp.run();
/*     */       
/*     */ 
/*  64 */       for (URL initialUrl : config.initialUrl) {
/*  65 */         if (config.reset) {
/*  66 */           Db.getInstance().reset(initialUrl.getHost());
/*     */         }
/*  68 */         Db.getInstance().addUrl(initialUrl);
/*     */       }
/*     */       
/*  71 */       stopOnIdle = (stopOnIdle) && (config.revisitAfter == -1);
/*     */     }
/*     */     
/*  74 */     logger.log(Level.INFO, "robots.txt processed, initial urls loaded");
/*     */     
/*  76 */     ArrayList<IWorker> workers = new ArrayList();
/*  77 */     ArrayList<Thread> threads = new ArrayList();
/*  78 */     for (HostsConfig config : configs) {
/*  79 */       Planner planner = new Planner(config, "[" + config.getSection() + "] Planner");
/*  80 */       planner.start();
/*  81 */       logger.log(Level.INFO, "Planner initialized, starting workers");
/*     */       
/*     */ 
/*  84 */       int workerNum = config.workers;
/*  85 */       logger.log(Level.INFO, "{0} worker{1} for {2}", new Object[] { Integer.valueOf(workerNum), workerNum > 1 ? "s" : "", config.getHosts().toString() });
/*  86 */       Class workerClass = crawler.core.Factory.getWorkerClass(config.module);
/*  87 */       Constructor workerConstructor = workerClass.getConstructor(new Class[0]);
/*     */       
/*     */ 
/*  90 */       for (int i = 0; i < workerNum; i++) {
/*  91 */         String workerName = "Worker-" + i;
/*  92 */         IWorker worker = (IWorker)workerConstructor.newInstance(new Object[0]);
/*  93 */         worker.setName(workerName);
/*  94 */         worker.init(planner, config);
/*  95 */         workers.add(worker);
/*  96 */         Thread t = new Thread(worker, "[" + config.getSection() + "] " + workerName);
/*  97 */         threads.add(t);
/*  98 */         t.start();
/*     */       }
/*     */     }
/*     */     
/* 102 */     logger.log(Level.INFO, "Workers started, starting main cycle");
/* 103 */     int idle = 0;
/* 104 */     while (idle != workers.size()) {
/* 105 */       idle = 0;
/* 106 */       for (int i = 0; i < workers.size(); i++) {
/* 107 */         if (!((Thread)threads.get(i)).isAlive())
/* 108 */           idle++;
/*     */       }
/* 110 */       Thread.sleep(1000L);
/*     */     }
/*     */   }
/*     */   
/*     */   protected static void setUpLogger() {
/* 115 */     logger.addHandler(new ConsoleHandler());
/* 116 */     logger.setUseParentHandlers(false);
/* 117 */     for (Handler handler : logger.getHandlers()) {
/* 118 */       if ((handler instanceof ConsoleHandler)) {
/* 119 */         handler.setLevel(Level.FINEST);
/*     */       }
/*     */       else {
/* 122 */         logger.removeHandler(handler);
/*     */       }
/*     */     }
/*     */   }
/*     */ }


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\Crawler.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */