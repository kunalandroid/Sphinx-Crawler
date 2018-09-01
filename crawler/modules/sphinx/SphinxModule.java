/*     */ package crawler.modules.sphinx;
/*     */ 
/*     */ import crawler.core.ConfigParser;
/*     */ import crawler.core.IModule;
/*     */ import crawler.db.Db;
/*     */ import crawler.db.H2Storage;
/*     */ import crawler.db.IStorage;
/*     */ import crawler.log.ConsoleHandler;
/*     */ import crawler.web.Page;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintStream;
/*     */ import java.net.MalformedURLException;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Statement;
/*     */ import java.util.ArrayList;
/*     */ import java.util.logging.Handler;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import java.util.zip.DataFormatException;
/*     */ import utils.Compressor;
/*     */ 
/*     */ 
/*     */ public class SphinxModule
/*     */   implements IModule
/*     */ {
/*  30 */   protected static final Logger logger = Logger.getLogger(SphinxModule.class.getName());
/*     */   
/*     */   public String getName()
/*     */   {
/*  34 */     return "sphinx";
/*     */   }
/*     */   
/*     */   public static void main(String[] args) throws IllegalAccessException, IllegalArgumentException, MalformedURLException, ClassNotFoundException, InstantiationException, SQLException {
/*  38 */     if (args.length == 0) {
/*  39 */       usage();
/*  40 */       return;
/*     */     }
/*     */     
/*  43 */     setUpLogger();
/*  44 */     ConfigParser configParser = new ConfigParser();
/*  45 */     configParser.setLogger(logger);
/*  46 */     ArrayList<crawler.modules.std.HostsConfig> configs = configParser.getConfigs();
/*     */     
/*  48 */     if ((args[0].equals("truncate")) && (args.length == 2)) {
/*  49 */       for (crawler.modules.std.HostsConfig config : configs) {
/*  50 */         if (config.getClass().equals(HostsConfig.class)) {
/*  51 */           HostsConfig sphinxConfig = (HostsConfig)config;
/*  52 */           Sphinx s = new Sphinx(sphinxConfig);
/*  53 */           s.init();
/*  54 */           s.truncate(args[1]);
/*  55 */           System.out.println("Succesfully truncated index \"" + sphinxConfig.sphinxIndex + "\"");
/*     */         }
/*     */         
/*     */       }
/*  59 */     } else if (args[0].equals("reimport")) {
/*  60 */       reimport(args, configs);
/*  61 */     } else if (args[0].equals("repair")) {
/*  62 */       repair(configs);
/*     */     } else {
/*  64 */       usage();
/*     */     }
/*     */   }
/*     */   
/*     */   protected static void setUpLogger() {
/*  69 */     logger.addHandler(new ConsoleHandler());
/*  70 */     logger.setLevel(Level.FINEST);
/*  71 */     logger.setUseParentHandlers(false);
/*  72 */     for (Handler handler : logger.getHandlers()) {
/*  73 */       if ((handler instanceof ConsoleHandler)) {
/*  74 */         handler.setLevel(Level.FINEST);
/*     */       }
/*     */       else {
/*  77 */         logger.removeHandler(handler);
/*     */       }
/*     */     }
/*     */   }
/*     */   
/*     */   protected static void usage() {
/*  83 */     System.out.println("Usage: SphinxModule truncate <hostname>");
/*     */   }
/*     */   
/*     */   protected static void reimport(String[] args, ArrayList<crawler.modules.std.HostsConfig> configs) {
/*     */     try {
/*  88 */       IStorage db = Db.getInstance();
/*  89 */       HostsConfig config = (HostsConfig)configs.get(0);
/*  90 */       Sphinx s = new Sphinx(config);
/*  91 */       s.init();
/*     */       
/*     */ 
/*  94 */       long lastId = 0L;
/*     */       ResultSet rs;
/*  96 */       do { rs = db.createStatement().executeQuery("SELECT * FROM urls WHERE fetched_at IS NOT NULL AND id > " + lastId + " ORDER BY id ASC LIMIT 500");
/*  97 */         while (rs.next()) {
/*  98 */           if (rs.isLast()) {
/*  99 */             lastId = rs.getLong("id");
/*     */           }
/*     */           
/* 102 */           if (((args.length < 2) || (!args[1].equals("hard"))) && (s.exists(rs.getLong("id")))) {
/* 103 */             logger.log(Level.FINE, "Exists: {0}, {1}", new Object[] { Long.valueOf(rs.getLong("id")), rs.getString("host") });
/*     */           }
/*     */           else
/*     */           {
/* 107 */             DataInputStream dis = null;
/*     */             try {
/* 109 */               Page page = Page.fromResultSet(rs);
/* 110 */               String fname = Processor.getFullFile(Processor.getFullPath(config, page), page);
/* 111 */               File file = new File(fname);
/* 112 */               if (!file.exists()) {
/* 113 */                 logger.log(Level.FINE, "File not found: {0}", Long.valueOf(rs.getLong("id")));
/* 114 */                 continue;
/*     */               }
/*     */               
/* 117 */               if (file.length() > 0L) {
/* 118 */                 byte[] fileData = new byte[(int)file.length()];
/* 119 */                 dis = new DataInputStream(new FileInputStream(file));
/* 120 */                 dis.readFully(fileData);
/* 121 */                 dis.close();
/*     */                 String content;
/*     */                 try
/*     */                 {
/* 125 */                   content = Compressor.decompress(fileData);
/*     */                 }
/*     */                 catch (DataFormatException ex)
/*     */                 {
/* 129 */                   Logger.getLogger(SphinxModule.class.getName()).log(Level.SEVERE, null, ex);
/* 130 */                   content = new String(fileData, "UTF-8");
/*     */                 }
/*     */                 
/* 133 */                 if (!content.isEmpty()) {
/* 134 */                   page.setContent(content);
/* 135 */                   s.addDocument(s.createDocument(rs, page));
/* 136 */                   logger.log(Level.FINE, "Inserted {0}, {1}", new Object[] { Long.valueOf(rs.getLong("id")), rs.getString("host") });
/*     */                 }
/*     */               } else {
/* 139 */                 db.resetById(rs.getLong("id"));
/* 140 */                 logger.log(Level.FINE, "Reset {0}, {1}", new Object[] { Long.valueOf(rs.getLong("id")), rs.getString("host") });
/*     */               }
/*     */             } catch (IOException ex) {
/* 143 */               Logger.getLogger(SphinxModule.class.getName()).log(Level.SEVERE, null, ex);
/*     */             }
/*     */           }
/*     */         }
/* 147 */       } while (rs.isAfterLast());
/*     */     } catch (SQLException|ClassNotFoundException|InstantiationException|IllegalAccessException ex) {
/* 149 */       Logger.getLogger(SphinxModule.class.getName()).log(Level.SEVERE, null, ex);
/*     */     }
/*     */   }
/*     */   
/*     */   protected static void repair(ArrayList<crawler.modules.std.HostsConfig> configs) {
/* 154 */     logger.log(Level.FINE, "Deleting duplicate pages");
/*     */     try {
/* 156 */       H2Storage db = (H2Storage)Db.getInstance();
/* 157 */       HostsConfig config = (HostsConfig)configs.get(0);
/* 158 */       Sphinx s = new Sphinx(config);
/* 159 */       s.init();
/*     */       
/* 161 */       long lastId = 0L;
/*     */       ResultSet rs;
/*     */       do {
/* 164 */         rs = db.createStatement().executeQuery("SELECT * FROM urls WHERE id > " + lastId + " ORDER BY id ASC LIMIT 500");
/* 165 */         while (rs.next()) {
/* 166 */           boolean processed = false;
/*     */           
/* 168 */           if (rs.isLast()) {
/* 169 */             lastId = rs.getLong("id");
/*     */           }
/* 171 */           ResultSet duplicates = db.createStatement().executeQuery("SELECT * FROM urls WHERE url = '" + rs.getString("url") + "' AND id <> " + rs.getInt("id"));
/* 172 */           while (duplicates.next()) {
/* 173 */             processed = true;
/* 174 */             s.deleteDocument(duplicates.getInt("id"));
/* 175 */             db.createStatement().execute("DELETE FROM urls WHERE id = " + duplicates.getInt("id"));
/* 176 */             logger.log(Level.SEVERE, "Deleted #{0}", Integer.valueOf(duplicates.getInt("id")));
/*     */           }
/* 178 */           if (!processed) {
/* 179 */             logger.log(Level.FINE, "Skipped #{0}", Integer.valueOf(rs.getInt("id")));
/*     */           }
/*     */         }
/* 182 */       } while (rs.isAfterLast());
/*     */     }
/*     */     catch (SQLException|ClassNotFoundException|InstantiationException|IllegalAccessException ex) {
/* 185 */       logger.log(Level.SEVERE, "Exception! {0}", ex.getMessage());
/*     */     }
/*     */   }
/*     */ }


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\modules\sphinx\SphinxModule.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */