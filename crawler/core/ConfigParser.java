/*     */ package crawler.core;
/*     */ 
/*     */ import crawler.modules.std.HostsConfig;
/*     */ import java.io.File;
/*     */ import java.lang.reflect.Field;
/*     */ import java.net.InetSocketAddress;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.Proxy;
/*     */ import java.net.Proxy.Type;
/*     */ import java.net.URL;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import org.apache.commons.configuration.ConfigurationException;
/*     */ import org.apache.commons.configuration.HierarchicalINIConfiguration;
/*     */ import org.apache.commons.configuration.SubnodeConfiguration;
/*     */ 
/*     */ 
/*     */ public class ConfigParser
/*     */ {
/*     */   protected Logger logger;
/*  23 */   protected String filename = "config/config.conf";
/*     */   
/*     */   public void setLogger(Logger logger) {
/*  26 */     this.logger = logger;
/*     */   }
/*     */   
/*     */   public void setFilename(String filename) {
/*  30 */     this.filename = filename;
/*     */   }
/*     */   
/*     */   public ArrayList<HostsConfig> getConfigs() throws IllegalArgumentException, IllegalAccessException, MalformedURLException, ClassNotFoundException, InstantiationException {
/*     */     try {
/*  35 */       HierarchicalINIConfiguration c = new HierarchicalINIConfiguration(new File(this.filename));
/*     */       
/*  37 */       this.logger.log(Level.INFO, "Using config file {0}", c.getFile().getAbsoluteFile());
/*     */       
/*     */ 
/*  40 */       for (Field f : Config.class.getFields()) {
/*  41 */         Class<?> t = f.getType();
/*  42 */         if (t == Integer.TYPE) {
/*  43 */           f.setInt(null, c.getSection(null).getInt(f.getName(), f.getInt(null)));
/*     */         }
/*  45 */         else if (t == String.class) {
/*  46 */           f.set(null, c.getSection(null).getString(f.getName(), (String)f.get(null)));
/*     */         }
/*  48 */         else if (t == Level.class) {
/*  49 */           String val = c.getSection(null).getString(f.getName());
/*  50 */           if (val != null) {
/*  51 */             Level lVal = Level.parse(val);
/*  52 */             f.set(null, lVal);
/*     */           }
/*     */         }
/*     */       }
/*     */       
/*     */ 
/*  58 */       ArrayList<HostsConfig> result = new ArrayList();
/*     */       
/*  60 */       for (String sectionName : c.getSections()) {
/*  61 */         if (sectionName != null) {
/*  62 */           if (!c.getSection(sectionName).getBoolean("enabled", true)) {
/*  63 */             this.logger.log(Level.FINER, "Section {0} disabled, skipping", sectionName);
/*     */           }
/*     */           else {
/*  66 */             String moduleName = c.getSection(sectionName).getString("module", HostsConfig.getDefaultModule());
/*  67 */             if (!Factory.moduleExists(moduleName))
/*  68 */               throw new ClassNotFoundException("Module \"" + moduleName + "\" not found. Aborting.");
/*  69 */             Class configClass = Factory.getHostsConfigClass(moduleName);
/*  70 */             HostsConfig cc = (HostsConfig)configClass.newInstance();
/*  71 */             cc.setSection(sectionName);
/*  72 */             setFieldsFromConfig(cc.getClass().getFields(), cc, c.getSection(sectionName));
/*     */             
/*  74 */             if (cc.initialUrl.size() > 0) {
/*  75 */               ArrayList<URL> ul = new ArrayList();
/*  76 */               for (Object url : cc.initialUrl) {
/*  77 */                 String urlStr = url.toString();
/*  78 */                 ul.add(new URL(urlStr));
/*     */               }
/*  80 */               cc.initialUrl = ul;
/*  81 */               cc.init();
/*  82 */               result.add(cc);
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*  87 */       return result;
/*     */     }
/*     */     catch (ConfigurationException ex) {
/*  90 */       this.logger.log(Level.SEVERE, null, ex);
/*     */     }
/*  92 */     return null;
/*     */   }
/*     */   
/*     */   protected static void setFieldsFromConfig(Field[] fields, Object obj, SubnodeConfiguration section) throws IllegalArgumentException, IllegalAccessException {
/*  96 */     setFieldsFromConfig(fields, obj, section, "");
/*     */   }
/*     */   
/*     */   protected static void setFieldsFromConfig(Field[] fields, Object obj, SubnodeConfiguration section, String prefix) throws IllegalArgumentException, IllegalAccessException {
/* 100 */     for (Field f : obj.getClass().getFields()) {
/* 101 */       String propName = prefix + "_" + f.getName();
/* 102 */       Class<?> t = f.getType();
/* 103 */       if (t == Integer.TYPE) {
/* 104 */         f.setInt(obj, section.getInt(propName, f.getInt(obj)));
/*     */       }
/* 106 */       else if (t == ArrayList.class) {
/* 107 */         String[] confSet = section.getStringArray(propName);
/* 108 */         ArrayList<Object> ls = (ArrayList)f.get(obj);
/* 109 */         if (ls == null) {
/* 110 */           ls = new ArrayList();
/*     */         }
/* 112 */         ls.addAll(Arrays.asList(confSet));
/* 113 */         f.set(obj, ls);
/*     */       }
/* 115 */       else if (t == Boolean.TYPE) {
/* 116 */         f.setBoolean(obj, section.getBoolean(propName, f.getBoolean(obj)));
/*     */       }
/* 118 */       else if (t == Proxy.class) {
/* 119 */         String proxyStr = section.getString(propName);
/* 120 */         if (proxyStr != null) {
/* 121 */           String[] proxyArr = proxyStr.split(":");
/* 122 */           f.set(obj, new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyArr[0], Integer.parseInt(proxyArr[1]))));
/*     */         }
/*     */       }
/* 125 */       else if (t == String.class) {
/* 126 */         f.set(obj, section.getString(propName, (String)f.get(obj)));
/*     */       }
/* 128 */       else if (t == Level.class) {
/* 129 */         String levelStr = section.getString(propName);
/* 130 */         if (levelStr != null) {
/* 131 */           f.set(obj, Level.parse(levelStr));
/*     */         }
/*     */       }
/* 134 */       else if ((f.get(obj) instanceof Object)) {
/* 135 */         setFieldsFromConfig(t.getFields(), f.get(obj), section, propName);
/*     */       }
/*     */     }
/*     */   }
/*     */ }


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\core\ConfigParser.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */