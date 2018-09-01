/*     */ package crawler.modules.sphinx;
/*     */ 
/*     */ import crawler.web.Page;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintStream;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.net.URL;
/*     */ import java.nio.ByteBuffer;
/*     */ import java.nio.CharBuffer;
/*     */ import java.nio.charset.CharacterCodingException;
/*     */ import java.nio.charset.Charset;
/*     */ import java.nio.charset.CharsetDecoder;
/*     */ import java.nio.charset.CodingErrorAction;
/*     */ import java.sql.Connection;
/*     */ import java.sql.DriverManager;
/*     */ import java.sql.PreparedStatement;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Statement;
/*     */ import java.sql.Timestamp;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import org.apache.commons.lang.StringEscapeUtils;
/*     */ import org.apache.commons.lang.StringUtils;
/*     */ import org.drizzle.jdbc.internal.mysql.MySQLProtocol;
/*     */ 
/*     */ public class Sphinx
/*     */ {
/*     */   protected Connection connection;
/*     */   protected HostsConfig config;
/*     */   
/*     */   public Sphinx(HostsConfig config)
/*     */   {
/*  36 */     this.config = config;
/*     */   }
/*     */   
/*     */   public void init() throws SQLException {
/*     */     try {
/*  41 */       Class.forName("org.drizzle.jdbc.DrizzleDriver");
/*     */     } catch (Exception e) {
/*  43 */       System.out.println(e.getClass() + "\n" + e.getMessage());
/*  44 */       System.exit(1);
/*     */     }
/*     */   }
/*     */   
/*     */   public Connection getConnection() throws SQLException {
/*  49 */     Connection localInstance = this.connection;
/*  50 */     if ((localInstance == null) || (localInstance.isClosed())) {
/*  51 */       synchronized (getClass()) {
/*  52 */         localInstance = this.connection;
/*  53 */         if ((localInstance == null) || (localInstance.isClosed())) {
/*  54 */           String connString = "jdbc:drizzle://" + this.config.sphinxHost + ":" + this.config.sphinxPort + "/dbname?characterEncoding=utf8";
/*  55 */           this.connection = (localInstance = DriverManager.getConnection(connString));
/*  56 */           Logger.getLogger(MySQLProtocol.class.getName()).setLevel(Level.OFF);
/*     */         }
/*     */       }
/*     */     }
/*  60 */     return localInstance;
/*     */   }
/*     */   
/*     */   public synchronized void reconnect() throws SQLException {
/*  64 */     this.connection = null;
/*  65 */     getConnection();
/*     */   }
/*     */   
/*     */   public boolean addDocument(Document doc)
/*     */   {
/*  70 */     String s = getStatementText("INSERT", doc);
/*     */     try {
/*  72 */       getConnection().createStatement().executeUpdate(s);
/*  73 */       int i = 1;
/*     */       
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*  86 */       return true;
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/*  75 */       e = 
/*     */       
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*  86 */         e;Logger.getLogger(Sphinx.class.getName()).log(Level.FINE, "INSERT FAILED \n{0}\n{1}\n{2}\n", new Object[] { doc.url, e.getMessage(), s });s = getStatementText("REPLACE", doc);
/*     */       try
/*     */       {
/*  79 */         getConnection().createStatement().executeUpdate(s);
/*     */       } catch (SQLException ex) {
/*  81 */         Logger.getLogger(Sphinx.class.getName()).log(Level.FINE, "REPLACE FAILED \n{0}\n{1}\n{2}\n", new Object[] { doc.url, ex, s });
/*  82 */         int j = 0;
/*     */         
/*     */ 
/*     */ 
/*  86 */         return true; } return true; } finally {} return true;
/*     */   }
/*     */   
/*     */   public boolean exists(long id) throws SQLException
/*     */   {
/*  91 */     ResultSet rs = getConnection().createStatement().executeQuery("SELECT id FROM " + this.config.sphinxIndex + " WHERE id = " + id);
/*  92 */     return rs.first();
/*     */   }
/*     */   
/*     */   protected String getStatementText(String type, Document doc) {
/*  96 */     String title = doc.title.replace("\\", " ").replace("'", " ").replace("\000", "");
/*  97 */     String content = doc.content.replace("\\", " ").replace("'", " ").replace("\000", "");
/*     */     try {
/*  99 */       CharsetDecoder utf8Decoder = Charset.forName("UTF-8").newDecoder();
/* 100 */       utf8Decoder.onMalformedInput(CodingErrorAction.IGNORE);
/* 101 */       utf8Decoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
/*     */       
/* 103 */       ByteBuffer bytes = ByteBuffer.wrap(title.getBytes("UTF-8"));
/* 104 */       title = utf8Decoder.decode(bytes).toString();
/*     */       
/* 106 */       bytes = ByteBuffer.wrap(content.getBytes("UTF-8"));
/* 107 */       content = utf8Decoder.decode(bytes).toString();
/*     */     }
/*     */     catch (UnsupportedEncodingException|CharacterCodingException ex) {}
/*     */     
/*     */ 
/* 112 */     return type + " INTO " + this.config.sphinxIndex + "(id, title, content, url, host, file, created_at, fetched_at) VALUES(" + doc.id + ", '" + title + "', '" + content + "', '" + StringEscapeUtils.escapeSql(doc.url) + "', '" + StringEscapeUtils.escapeSql(doc.host) + "', '" + StringEscapeUtils.escapeSql(doc.file) + "', " + doc.created_at.getTime() / 1000L + ", " + doc.fetched_at.getTime() / 1000L + ")";
/*     */   }
/*     */   
/*     */   public void deleteDocument(int id) throws SQLException {
/* 116 */     getConnection().createStatement().executeUpdate("DELETE FROM " + this.config.sphinxIndex + " WHERE id = " + id);
/*     */   }
/*     */   
/*     */   public void truncate(String host) throws SQLException {
/* 120 */     ResultSet rs = getConnection().createStatement().executeQuery("SELECT * FROM " + this.config.sphinxIndex + " WHERE MATCH('@host " + host + "') LIMIT 1000");
/*     */     
/* 122 */     ArrayList<Integer> ids = new ArrayList();
/* 123 */     while (rs.next()) {
/* 124 */       if (rs.getString("host").equals(host)) {
/* 125 */         ids.add(Integer.valueOf(rs.getInt("id")));
/*     */       }
/*     */     }
/* 128 */     if (ids.size() > 0) {
/* 129 */       getConnection().createStatement().executeUpdate("DELETE FROM " + this.config.sphinxIndex + " WHERE id IN (" + StringUtils.join(ids, ", ") + ")");
/* 130 */       truncate(host);
/*     */     }
/*     */   }
/*     */   
/*     */   protected void setParams(PreparedStatement s, Document doc) throws SQLException {
/* 135 */     int i = 1;
/* 136 */     s.setLong(i++, doc.id);
/* 137 */     s.setString(i++, doc.title);
/* 138 */     s.setString(i++, doc.url);
/* 139 */     s.setString(i++, doc.host);
/* 140 */     s.setString(i++, doc.file);
/* 141 */     s.setString(i++, doc.filename);
/* 142 */     s.setTimestamp(i++, doc.created_at);
/* 143 */     s.setTimestamp(i++, doc.fetched_at);
/*     */   }
/*     */   
/*     */   public Document createDocument(ResultSet data, Page page) throws SQLException {
/* 147 */     Document doc = new Document();
/* 148 */     doc.id = data.getLong("id");
/* 149 */     doc.title = page.getTitle();
/* 150 */     doc.content = page.getContent();
/* 151 */     doc.url = data.getString("url");
/* 152 */     doc.host = page.getUrl().getHost();
/* 153 */     doc.file = page.getFileName();
/* 154 */     doc.created_at = data.getTimestamp("created_at");
/* 155 */     doc.fetched_at = (data.getTimestamp("fetched_at") == null ? new Timestamp(new Date().getTime()) : data.getTimestamp("fetched_at"));
/*     */     
/* 157 */     return doc;
/*     */   }
/*     */ }


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\modules\sphinx\Sphinx.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */