/*    */ package utils;
/*    */ 
/*    */ import java.io.ByteArrayOutputStream;
/*    */ import java.io.IOException;
/*    */ import java.io.UnsupportedEncodingException;
/*    */ import java.util.logging.Level;
/*    */ import java.util.logging.Logger;
/*    */ import java.util.zip.DataFormatException;
/*    */ import java.util.zip.Deflater;
/*    */ import java.util.zip.Inflater;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class Compressor
/*    */ {
/*    */   public static byte[] compress(String data)
/*    */   {
/*    */     try
/*    */     {
/* 22 */       Deflater compressor = new Deflater(7);
/* 23 */       byte[] input = data.getBytes("UTF-8");
/* 24 */       byte[] buf = new byte['က'];
/*    */       
/* 26 */       compressor.setInput(input);
/* 27 */       compressor.finish();
/* 28 */       ByteArrayOutputStream outputStream = new ByteArrayOutputStream(buf.length);
/* 29 */       while (!compressor.finished()) {
/* 30 */         int compressedDataLength = compressor.deflate(buf);
/* 31 */         outputStream.write(buf, 0, compressedDataLength);
/*    */       }
/* 33 */       outputStream.close();
/* 34 */       compressor.end();
/*    */       
/* 36 */       return outputStream.toByteArray();
/*    */     }
/*    */     catch (UnsupportedEncodingException ex) {
/* 39 */       Logger.getLogger("").log(Level.SEVERE, null, ex);
/* 40 */       return data.getBytes();
/*    */     } catch (IOException ex) {
/* 42 */       Logger.getLogger("").log(Level.SEVERE, null, ex); }
/* 43 */     return data.getBytes();
/*    */   }
/*    */   
/*    */   public static String decompress(byte[] input) throws DataFormatException, IOException
/*    */   {
/* 48 */     Inflater decompressor = new Inflater();
/* 49 */     decompressor.setInput(input);
/*    */     
/* 51 */     byte[] buf = new byte['က'];
/* 52 */     ByteArrayOutputStream outputStream = new ByteArrayOutputStream(input.length);
/* 53 */     while (!decompressor.finished()) {
/* 54 */       int len = decompressor.inflate(buf);
/* 55 */       outputStream.write(buf, 0, len);
/*    */     }
/*    */     
/* 58 */     outputStream.close();
/* 59 */     return new String(outputStream.toByteArray(), "UTF-8");
/*    */   }
/*    */ }


/* Location:              E:\kunalandroid\Sphinx-Crawler\crawler.jar!\utils\Compressor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */