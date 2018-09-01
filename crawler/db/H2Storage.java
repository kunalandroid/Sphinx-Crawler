/*     */ package crawler.db;
/*     */ 
/*     */ import crawler.core.Config;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.InputStreamReader;
/*     */ import java.net.URL;
/*     */ import java.sql.Connection;
/*     */ import java.sql.PreparedStatement;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Statement;
/*     */ import java.sql.Timestamp;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import org.h2.jdbcx.JdbcDataSource;
/*     */ 
/*     */ public final class H2Storage implements IStorage
/*     */ {
/*     */   protected Connection connection;
/*     */   
/*     */   public H2Storage() throws SQLException
/*     */   {
/*  25 */     createConnection();
/*     */   }
/*     */   
/*     */   protected void createConnection() throws SQLException {
/*  29 */     JdbcDataSource ds = new JdbcDataSource();
/*  30 */     ds.setURL(Config.connectionString);
/*  31 */     ds.setUser(Config.user);
/*  32 */     ds.setPassword(Config.password);
/*  33 */     this.connection = ds.getConnection();
/*  34 */     createTables();
/*     */   }
/*     */   
/*     */   public Connection getConnection() throws SQLException {
/*  38 */     if (this.connection.isClosed()) {
/*  39 */       createConnection();
/*     */     }
/*  41 */     return this.connection;
/*     */   }
/*     */   
/*     */   public Statement createStatement() throws SQLException
/*     */   {
/*  46 */     return getConnection().createStatement();
/*     */   }
/*     */   
/*     */   private void createTables() {
/*     */     try {
/*  51 */       String filename = "/crawler/db/structure.sql";
/*  52 */       BufferedReader ir = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename)));
/*     */       
/*     */ 
/*  55 */       StringBuilder sb = new StringBuilder();
/*  56 */       String inputLine; while ((inputLine = ir.readLine()) != null) {
/*  57 */         sb.append(inputLine);
/*     */       }
/*     */       
/*  60 */       getConnection().createStatement().executeUpdate(sb.toString());
/*     */     } catch (java.io.IOException|SQLException ex) {
/*  62 */       Logger.getLogger(H2Storage.class.getName()).log(Level.SEVERE, ex.getMessage());
/*     */     }
/*     */   }
/*     */   
/*     */   public void addUrl(URL url)
/*     */   {
/*     */     try {
/*  69 */       insertUrl(url);
/*     */     } catch (SQLException e) {
/*  71 */       if (e.getErrorCode() != 23505) {
/*  72 */         Logger.getLogger(IStorage.class.getName()).log(Level.SEVERE, "SQL exception: {0}, {1}", new Object[] { Integer.valueOf(e.getErrorCode()), e.getMessage() });
/*     */       }
/*     */     }
/*     */   }
/*     */   
/*     */   protected void insertUrl(URL url) throws SQLException
/*     */   {
/*  79 */     PreparedStatement s = getConnection().prepareStatement("INSERT INTO urls(url, host, created_at, fetched_at) VALUES(?, ?, ?, null)");
/*  80 */     s.setString(1, url.toString());
/*  81 */     s.setString(2, url.getHost());
/*  82 */     s.setTimestamp(3, new Timestamp(new Date().getTime()));
/*  83 */     s.execute();
/*     */   }
/*     */   
/*     */   public void logVisit(int responseCode, URL url, String hash)
/*     */   {
/*     */     try {
/*  89 */       PreparedStatement s = getConnection().prepareStatement("UPDATE urls SET fetched_at = NOW()" + (hash != null ? ", hash = ?" : "") + " WHERE url = ?");
/*     */       
/*  91 */       int i = 1;
/*  92 */       if (hash != null) {
/*  93 */         s.setString(i++, hash);
/*     */       }
/*     */       
/*  96 */       s.setString(i++, url.toString());
/*  97 */       s.execute();
/*     */     }
/*     */     catch (SQLException ex) {
/* 100 */       Logger.getLogger(IStorage.class.getName()).log(Level.SEVERE, null, ex);
/*     */     }
/*     */   }
/*     */   
/*     */   public String[] preloadQueue(String host, int period, int limit, ArrayList<String> skipUrls)
/*     */   {
/*     */     try {
/* 107 */       String statement = "SELECT url FROM urls WHERE host = ?" + (skipUrls.size() > 0 ? " AND url NOT IN (" + preparePlaceHolders(skipUrls.size()) + ")" : "") + " AND (fetched_at IS NULL " + (period > -1 ? " OR fetched_at < ?" : "") + ") ORDER BY fetched_at ASC NULLS FIRST, created_at ASC LIMIT ?";
/* 108 */       PreparedStatement s = getConnection().prepareStatement(statement);
/* 109 */       s.setString(1, host);
/* 110 */       setValues(s, 2, skipUrls.toArray());
/*     */       
/* 112 */       int i = skipUrls.size() + 2;
/* 113 */       if (period > -1) {
/* 114 */         Date d = new Date(new Date().getTime() - period * 1000);
/* 115 */         s.setTimestamp(i, new Timestamp(d.getTime()));
/* 116 */         i++;
/*     */       }
/* 118 */       s.setInt(i, limit);
/*     */       
/* 120 */       ResultSet rs = s.executeQuery();
/* 121 */       ArrayList<String> result = new ArrayList();
/*     */       
/* 123 */       while (rs.next()) {
/* 124 */         result.add(rs.getString("url"));
/*     */       }
/*     */       
/* 127 */       String[] strResult = new String[result.size()];
/* 128 */       i = 0;
/* 129 */       for (String str : result) {
/* 130 */         strResult[(i++)] = str;
/*     */       }
/*     */       
/* 133 */       s.close();
/*     */       
/* 135 */       return strResult;
/*     */     }
/*     */     catch (SQLException ex) {
/* 138 */       Logger.getLogger(H2Storage.class.getName()).log(Level.SEVERE, null, ex);
/*     */     }
/*     */     
/* 141 */     return new String[0];
/*     */   }
/*     */   
/*     */   public void reset(String host)
/*     */   {
/*     */     try {
/* 147 */       getConnection().createStatement().execute("UPDATE urls SET fetched_at = null, hash = null WHERE host = '" + host + "'");
/*     */     } catch (SQLException ex) {
/* 149 */       Logger.getLogger(H2Storage.class.getName()).log(Level.SEVERE, null, ex);
/*     */     }
/*     */   }
/*     */   
/*     */   public void resetById(long id)
/*     */   {
/*     */     try {
/* 156 */       getConnection().createStatement().execute("UPDATE urls SET fetched_at = null, hash = null WHERE id = " + id);
/*     */     } catch (SQLException ex) {
/* 158 */       Logger.getLogger(H2Storage.class.getName()).log(Level.SEVERE, null, ex);
/*     */     }
/*     */   }
/*     */   
/*     */ 
/*     */   public void ensureTables() {}
/*     */   
/*     */   public void truncate()
/*     */     throws SQLException
/*     */   {
/* 168 */     getConnection().createStatement().execute("TRUNCATE TABLE urls");
/*     */   }
/*     */   
/*     */   public int countByHash(String hash)
/*     */   {
/*     */     try {
/* 174 */       ResultSet rs = getConnection().createStatement().executeQuery("SELECT COUNT(*) FROM urls WHERE hash = '" + hash + "'");
/* 175 */       rs.next();
/* 176 */       return rs.getInt(1);
/*     */     } catch (SQLException e) {
/* 178 */       Logger.getLogger(H2Storage.class.getName()).log(Level.SEVERE, null, e); }
/* 179 */     return 0;
/*     */   }
/*     */   
/*     */   public static String preparePlaceHolders(int length)
/*     */   {
/* 184 */     StringBuilder builder = new StringBuilder();
/* 185 */     for (int i = 0; i < length;) {
/* 186 */       builder.append("?");
/* 187 */       i++; if (i < length) {
/* 188 */         builder.append(",");
/*     */       }
/*     */     }
/* 191 */     return builder.toString();
/*     */   }
/*     */   
/*     */   public static void setValues(PreparedStatement preparedStatement, int startIndex, Object[] values) throws SQLException {
/* 195 */     for (int i = 0; i < values.length; i++) {
/* 196 */       preparedStatement.setObject(startIndex + i, values[i]);
/*     */     }
/*     */   }
/*     */   
/*     */   public void deleteUrl(URL url) throws SQLException
/*     */   {
/* 202 */     getConnection().createStatement().executeUpdate("DELETE FROM urls WHERE url = '" + url.toString() + "' LIMIT 1");
/*     */   }
/*     */   
/*     */   public ResultSet getDataByUrl(URL url)
/*     */   {
/*     */     try {
/* 208 */       PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM urls WHERE url = ?");
/* 209 */       ps.setString(1, url.toString());
/* 210 */       ResultSet rs = ps.executeQuery();
/* 211 */       return rs.first() ? rs : null;
/*     */     } catch (SQLException ex) {}
/* 213 */     return null;
/*     */   }
/*     */ }


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\crawler\db\H2Storage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */