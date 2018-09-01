/*     */ package crawler.modules.std;
/*     */ 
/*     */ import crawler.core.IWorkerMonitor;
/*     */ import crawler.log.ConsoleHandler;
/*     */ import crawler.planner.IPlanner;
/*     */ import crawler.processor.IPageProcessor;
/*     */ import crawler.web.Page;
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.net.HttpURLConnection;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.URL;
/*     */ import java.sql.SQLException;
/*     */ import java.util.HashMap;
/*     */ import java.util.logging.Handler;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import org.mozilla.universalchardet.UniversalDetector;
/*     */ 
/*     */ public class Worker implements crawler.core.IWorker
/*     */ {
/*     */   protected URL initialUrl;
/*     */   protected URL url;
/*     */   protected HttpURLConnection connection;
/*     */   protected String encoding;
/*     */   protected String content;
/*     */   protected boolean retry;
/*     */   protected int retriesCount;
/*     */   protected IPlanner planner;
/*     */   protected HostsConfig config;
/*     */   protected HashMap<String, HostsConfig> configsByHost;
/*     */   protected int processedUrlCount;
/*     */   protected boolean isIdle;
/*     */   protected IPageProcessor processor;
/*     */   public Logger logger;
/*  38 */   protected static HashMap<String, Long> prevTimes = new HashMap();
/*  39 */   protected static HashMap<String, Long> delays = new HashMap();
/*     */   private String name;
/*     */   
/*     */   public Worker()
/*     */   {
/*  28 */     this.retry = false;
/*  29 */     this.retriesCount = 0;
/*     */     
/*     */ 
/*     */ 
/*  33 */     this.configsByHost = new HashMap();
/*  34 */     this.processedUrlCount = 0;
/*  35 */     this.isIdle = false;
/*     */     
/*  37 */     this.logger = Logger.getLogger(Worker.class.getName() + hashCode());
/*     */     
/*     */ 
/*  40 */     this.name = "Worker";
/*     */   }
/*     */   
/*     */   public void init(IPlanner planner, HostsConfig config) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
/*  44 */     this.planner = planner;
/*  45 */     this.config = config;
/*     */     
/*  47 */     setLogLevel();
/*  48 */     this.processor = ((IPageProcessor)crawler.core.Factory.getProcessorClass(config.getModule()).newInstance());
/*  49 */     this.processor.init(config, planner);
/*  50 */     for (String host : config.getHosts()) {
/*  51 */       this.configsByHost.put(host, config);
/*  52 */       delays.put(host, Long.valueOf(1000 / config.maxRate));
/*  53 */       prevTimes.put(host, Long.valueOf(0L));
/*     */     }
/*     */   }
/*     */   
/*     */   protected void setLogLevel() {
/*  58 */     this.logger.setLevel(this.config.logLevel);
/*  59 */     this.logger.setUseParentHandlers(false);
/*  60 */     this.logger.addHandler(new ConsoleHandler());
/*  61 */     for (Handler handler : this.logger.getHandlers()) {
/*  62 */       if ((handler instanceof ConsoleHandler)) {
/*  63 */         handler.setLevel(this.config.logLevel);
/*     */       }
/*     */       else {
/*  66 */         this.logger.removeHandler(handler);
/*     */       }
/*     */     }
/*     */   }
/*     */   
/*     */   public void run()
/*     */   {
/*  73 */     boolean doLimitRate = true;
/*     */     for (;;) {
/*     */       try {
/*  76 */         checkStop();
/*  77 */         reset();
/*     */         
/*     */ 
/*  80 */         if (!this.retry) {
/*  81 */           getUrl();
/*  82 */           this.processedUrlCount += 1;
/*     */           
/*  84 */           if (!getProcessor().isUrlSuitable(this.url)) {
/*  85 */             this.logger.log(Level.FINER, "Deleting previously collected url: {0}", this.url);
/*  86 */             this.processor.deleteUrl(this.url);
/*  87 */             onVisitFinished();
/*  88 */             continue;
/*     */           }
/*     */         }
/*     */         
/*  92 */         onVisitStarted();
/*  93 */         if (doLimitRate) {
/*  94 */           limitRate(this.initialUrl.getHost());
/*     */         }
/*  96 */         this.isIdle = false;
/*     */         
/*  98 */         checkRobots();
/*  99 */         doLimitRate = true;
/* 100 */         connect();
/* 101 */         checkConnectionData();
/* 102 */         fetch();
/* 103 */         this.logger.log(Level.FINEST, "Creating page for {0}", this.url.toString());
/* 104 */         Page page = new Page(this.url, this.content);
/* 105 */         processContent(page);
/*     */         
/* 107 */         this.planner.registerSuccess(this.url);
/* 108 */         getProcessor().logVisit(this.connection.getResponseCode(), page);
/* 109 */         if (!this.url.equals(this.initialUrl)) {
/* 110 */           getProcessor().logVisit(this.initialUrl, page);
/*     */         }
/* 112 */         this.retry = false;
/* 113 */         this.retriesCount = 0;
/*     */ 
/*     */       }
/*     */       catch (CrawlerStopException ex)
/*     */       {
/* 118 */         onStop();
/* 119 */         return;
/*     */       }
/*     */       catch (FileNotFoundException e)
/*     */       {
/* 123 */         this.logger.log(Level.FINER, "Url not found: {0}", this.url);
/* 124 */         this.retry = false;
/*     */         try {
/* 126 */           getProcessor().logVisit(this.connection.getResponseCode(), this.url);
/* 127 */           getProcessor().logVisit(this.connection.getResponseCode(), this.initialUrl);
/* 128 */           this.planner.registerSuccess(this.url);
/* 129 */           if (!this.url.equals(this.initialUrl))
/* 130 */             this.planner.registerSuccess(this.initialUrl);
/*     */         } catch (IOException ex) {
/* 132 */           this.logger.log(Level.SEVERE, null, ex);
/*     */         }
/*     */       }
/*     */       catch (MalformedURLException ex)
/*     */       {
/* 137 */         this.logger.log(Level.WARNING, "Malformed URL: {0}", this.url);
/* 138 */         getProcessor().logVisit(this.url);
/*     */       }
/*     */       catch (IOException ex)
/*     */       {
/*     */         try {
/* 143 */           int rc = this.connection.getResponseCode();
/* 144 */           if ((rc == 502) || (rc == 503) || (rc == 504)) {
/* 145 */             this.retriesCount += 1;
/* 146 */             this.retry = true;
/* 147 */             if (this.retriesCount == ((HostsConfig)this.configsByHost.get(this.url.getHost())).maxRetries) {
/* 148 */               this.retry = false;
/* 149 */               this.retriesCount = 0;
/* 150 */               this.logger.log(Level.WARNING, "Reached maxRetries: {0}", this.url);
/* 151 */               getProcessor().logVisit(rc, this.url);
/* 152 */               this.planner.registerFail(this.url);
/*     */             }
/*     */             else {
/* 155 */               this.logger.log(Level.WARNING, "Got " + rc + ", retrying {0}", this.url);
/*     */             }
/*     */           } else {
/* 158 */             getProcessor().logVisit(this.url);
/* 159 */             this.planner.registerSuccess(this.url);
/* 160 */             this.logger.log(Level.WARNING, ex.getMessage());
/*     */           }
/*     */         } catch (IOException ex1) {
/* 163 */           Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex1);
/*     */         }
/*     */       }
/*     */       catch (InterruptedException ex)
/*     */       {
/* 168 */         this.retry = false;
/* 169 */         getProcessor().logVisit(this.url);
/* 170 */         this.logger.log(Level.SEVERE, null, ex);
/*     */ 
/*     */       }
/*     */       catch (CrawlerEmptyQueueException ex)
/*     */       {
/* 175 */         this.retry = false;
/* 176 */         this.logger.log(Level.FINEST, "[{0}] [{1}] Queue is empty", new Object[] { this.config.getSection(), getName() });
/*     */         
/*     */ 
/* 179 */         this.isIdle = true;
/*     */         try {
/* 181 */           synchronized (getMonitor()) {
/* 182 */             getMonitor().wait();
/*     */           }
/*     */         } catch (InterruptedException ex1) {
/* 185 */           this.logger.log(Level.SEVERE, null, ex1);
/*     */         }
/*     */         
/*     */       }
/*     */       catch (CrawlerRobotsDisabledException ex)
/*     */       {
/* 191 */         this.retry = false;
/* 192 */         getProcessor().logVisit(this.url);
/* 193 */         doLimitRate = false;
/* 194 */         this.logger.log(Level.FINER, "URL disabled in robots.txt: {0}", this.url);
/*     */ 
/*     */       }
/*     */       catch (CrawlerTooManyredirectsException ex)
/*     */       {
/* 199 */         this.retry = false;
/* 200 */         getProcessor().logVisit(this.initialUrl);
/* 201 */         this.logger.log(Level.WARNING, "Too many redirects: {0}", this.url);
/*     */ 
/*     */       }
/*     */       catch (CrawlerPolicyException ex)
/*     */       {
/* 206 */         this.retry = false;
/* 207 */         getProcessor().logVisit(this.initialUrl);
/* 208 */         getProcessor().logVisit(this.url);
/* 209 */         this.logger.log(Level.WARNING, "Policy check failed on redirect: {0}", this.url);
/*     */       }
/*     */       catch (CrawlerNoConnectionException ex)
/*     */       {
/* 213 */         this.retry = false;
/* 214 */         this.logger.log(Level.WARNING, "No connection: {0}", this.url);
/*     */ 
/*     */       }
/*     */       catch (CrawlerContentTypeException ex)
/*     */       {
/* 219 */         this.retry = false;
/* 220 */         this.logger.log(Level.FINER, "Content-Type/Length/Range mismatch: {0}", this.url);
/*     */         try
/*     */         {
/* 223 */           getProcessor().logVisit(this.connection.getResponseCode(), this.url);
/* 224 */           if (!this.url.equals(this.initialUrl)) {
/* 225 */             getProcessor().logVisit(this.connection.getResponseCode(), this.initialUrl);
/*     */           }
/*     */         } catch (IOException ex1) {
/* 228 */           this.logger.log(Level.SEVERE, null, ex1);
/*     */         }
/*     */         
/*     */       }
/*     */       catch (CrawlerSizeExceededException ex)
/*     */       {
/* 234 */         this.retry = false;
/* 235 */         this.logger.log(Level.FINER, "Size limit exceeded: {0}", this.url);
/* 236 */         getProcessor().logVisit(this.initialUrl);
/* 237 */         getProcessor().logVisit(this.url);
/*     */       }
/*     */       
/* 240 */       onVisitFinished();
/*     */     }
/*     */   }
/*     */   
/*     */   public boolean isIdle()
/*     */   {
/* 246 */     return this.isIdle;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public void checkConnectionData()
/*     */     throws Worker.CrawlerContentTypeException
/*     */   {
/* 256 */     if (!getProcessor().isConnectionSuitable(this.connection)) {
/* 257 */       throw new CrawlerContentTypeException();
/*     */     }
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public void processContent(Page page)
/*     */     throws IOException
/*     */   {
/* 268 */     getProcessor().processContent(this.connection.getResponseCode(), page);
/*     */   }
/*     */   
/*     */   protected void checkRobots() throws Worker.CrawlerRobotsDisabledException {
/* 272 */     if (!getProcessor().checkRobots(this.url)) {
/* 273 */       throw new CrawlerRobotsDisabledException();
/*     */     }
/*     */   }
/*     */   
/*     */   public void checkStop() throws Worker.CrawlerStopException {
/* 278 */     if ((getMonitor().getIsStop()) || (!getMonitor().isAlive()))
/* 279 */       throw new CrawlerStopException();
/*     */   }
/*     */   
/*     */   protected void getUrl() throws InterruptedException, Worker.CrawlerEmptyQueueException {
/* 283 */     this.url = this.planner.getUrl();
/* 284 */     this.initialUrl = this.url;
/*     */     
/*     */ 
/* 287 */     if (this.url == null)
/* 288 */       throw new CrawlerEmptyQueueException();
/* 289 */     if (!this.planner.hostEnabled(this.url.getHost())) {
/* 290 */       getUrl();
/*     */     }
/*     */   }
/*     */   
/*     */   public void connect() throws Worker.CrawlerTooManyredirectsException, IOException, Worker.CrawlerPolicyException, Worker.CrawlerNoConnectionException {
/* 295 */     java.net.Proxy proxy = getConfig().proxy;
/*     */     
/* 297 */     int redirectNum = 0;
/*     */     String location;
/*     */     do
/*     */     {
/* 301 */       if (redirectNum == getConfig().maxRedirects) {
/* 302 */         throw new CrawlerTooManyredirectsException();
/*     */       }
/*     */       
/* 305 */       this.connection = ((HttpURLConnection)(proxy != null ? this.url.openConnection(proxy) : this.url.openConnection()));
/* 306 */       this.connection.setInstanceFollowRedirects(false);
/* 307 */       this.connection.connect();
/* 308 */       location = this.connection.getHeaderField("Location");
/*     */       
/* 310 */       if (location != null)
/*     */       {
/* 312 */         getProcessor().logVisit(this.connection.getResponseCode(), this.url);
/* 313 */         onVisitFinished();
/* 314 */         this.url = crawler.web.URLManager.createUrl(this.url, location);
/* 315 */         if (!this.planner.isVisitAllowed(this.url)) {
/* 316 */           throw new CrawlerPolicyException();
/*     */         }
/* 318 */         onVisitStarted();
/*     */       }
/* 320 */       redirectNum++;
/* 321 */     } while (location != null);
/*     */     
/* 323 */     if (!this.url.equals(this.initialUrl)) {
/* 324 */       getProcessor().addUrl(this.url);
/*     */     }
/*     */     
/* 327 */     if (this.connection == null)
/* 328 */       throw new CrawlerNoConnectionException();
/*     */   }
/*     */   
/*     */   protected void reset() {
/* 332 */     if (!this.retry) {
/* 333 */       this.url = null;
/* 334 */       this.initialUrl = null;
/*     */     }
/* 336 */     this.connection = null;
/* 337 */     this.encoding = null;
/* 338 */     this.content = null;
/*     */   }
/*     */   
/*     */   protected void fetch() throws IOException, Worker.CrawlerSizeExceededException {
/* 342 */     Object[] logParams = { Integer.valueOf(this.processedUrlCount), this.url };
/* 343 */     this.logger.log(Level.FINE, "Visiting #{0}: {1}", logParams);
/*     */     
/* 345 */     String[] result = fetchContent(this.connection, this.config.maxSize);
/* 346 */     this.content = result[0];
/* 347 */     this.encoding = result[1];
/* 348 */     this.logger.log(Level.FINEST, "Detected encoding {0} for {1} ({2})", new Object[] { this.encoding, this.url, utils.Md5.getHash(this.url.toString()) });
/*     */   }
/*     */   
/*     */   public static String[] fetchContent(HttpURLConnection connection) throws IOException, Worker.CrawlerSizeExceededException {
/* 352 */     return fetchContent(connection, -1);
/*     */   }
/*     */   
/*     */   public static String[] fetchContent(HttpURLConnection connection, int maxSize) throws IOException, Worker.CrawlerSizeExceededException {
/* 356 */     String[] result = new String[2];
/* 357 */     InputStream in = connection.getInputStream();
/* 358 */     UniversalDetector detector = new UniversalDetector(null);
/* 359 */     ByteArrayOutputStream out = new ByteArrayOutputStream();
/*     */     
/* 361 */     byte[] outBuf = new byte['က'];
/*     */     
/* 363 */     int totalReadLen = 0;
/* 364 */     int readLen; while ((readLen = in.read(outBuf)) != -1)
/*     */     {
/* 366 */       if ((maxSize != -1) && (totalReadLen >= maxSize)) {
/* 367 */         throw new CrawlerSizeExceededException();
/*     */       }
/* 369 */       totalReadLen += readLen;
/*     */       
/* 371 */       if (!detector.isDone()) {
/* 372 */         detector.handleData(outBuf, 0, readLen);
/*     */       }
/* 374 */       out.write(outBuf, 0, readLen);
/*     */     }
/* 376 */     detector.dataEnd();
/* 377 */     in.close();
/*     */     
/* 379 */     result[1] = detector.getDetectedCharset();
/*     */     
/* 381 */     detector.reset();
/* 382 */     result[0] = (result[1] != null ? new String(out.toByteArray(), result[1]) : new String(out.toByteArray()));
/*     */     
/* 384 */     return result;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   protected void limitRate(String host)
/*     */   {
/* 391 */     synchronized ((HostsConfig)this.configsByHost.get(host)) {
/* 392 */       Long pTime = (Long)prevTimes.get(host);
/* 393 */       Long delay = (Long)delays.get(host);
/*     */       
/* 395 */       if (System.currentTimeMillis() - pTime.longValue() < delay.longValue()) {
/* 396 */         long waitTime = delay.longValue() - (System.currentTimeMillis() - pTime.longValue());
/*     */         try
/*     */         {
/* 399 */           Thread.sleep(waitTime);
/*     */         }
/*     */         catch (InterruptedException ex) {
/* 402 */           Logger.getLogger(crawler.core.Planner.class.getName()).log(Level.SEVERE, null, ex);
/*     */         }
/*     */       }
/*     */       
/* 406 */       prevTimes.put(host, Long.valueOf(System.currentTimeMillis()));
/*     */     }
/*     */   }
/*     */   
/*     */   protected IPageProcessor getProcessor() {
/* 411 */     return this.processor;
/*     */   }
/*     */   
/*     */   protected HostsConfig getConfig() {
/* 415 */     return (HostsConfig)this.configsByHost.get(this.url.getHost());
/*     */   }
/*     */   
/*     */   protected void onStop() {}
/*     */   
/*     */   protected void onVisitStarted()
/*     */   {
/* 422 */     if (this.url != null)
/* 423 */       this.planner.setInProgress(this.url);
/*     */   }
/*     */   
/*     */   protected void onVisitFinished() {
/* 427 */     if (this.url != null) {
/* 428 */       this.planner.removeInProgress(this.url);
/* 429 */       if (!this.initialUrl.equals(this.url)) {
/* 430 */         this.planner.removeInProgress(this.initialUrl);
/*     */       }
/*     */     }
/*     */   }
/*     */   
/*     */   public IWorkerMonitor getMonitor()
/*     */   {
/* 437 */     return this.planner;
/*     */   }
/*     */   
/*     */   public void setName(String n)
/*     */   {
/* 442 */     this.name = n;
/*     */   }
/*     */   
/*     */   public String getName()
/*     */   {
/* 447 */     return this.name;
/*     */   }
/*     */   
/*     */   public static class CrawlerSizeExceededException
/*     */     extends Exception
/*     */   {}
/*     */   
/*     */   public static class CrawlerNoConnectionException
/*     */     extends Exception
/*     */   {}
/*     */   
/*     */   public static class CrawlerContentTypeException
/*     */     extends Worker.CrawlerURLException
/*     */   {}
/*     */   
/*     */   public static class CrawlerPolicyException
/*     */     extends Worker.CrawlerURLException
/*     */   {}
/*     */   
/*     */   public static class CrawlerTooManyredirectsException
/*     */     extends Worker.CrawlerURLException
/*     */   {}
/*     */   
/*     */   public static class CrawlerRobotsDisabledException
/*     */     extends Exception
/*     */   {}
/*     */   
/*     */   public static class CrawlerURLException
/*     */     extends Exception
/*     */   {}
/*     */   
/*     */   public static class CrawlerEmptyQueueException
/*     */     extends Exception
/*     */   {}
/*     */   
/*     */   public static class CrawlerStopException
/*     */     extends Exception
/*     */   {}
/*     */ }


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\modules\std\Worker.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */