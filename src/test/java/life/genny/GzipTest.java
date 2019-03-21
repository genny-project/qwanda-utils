package life.genny;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;


import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.gson.Gson;

import io.vertx.core.json.JsonObject;

public class GzipTest {

	@Test
	public void zipTest()
	{
		String originalStr = "This is a test!";
		byte[] zippedData = null;
		try {
			zippedData = zipped(originalStr);
			String js = compressAndEncodeString(originalStr);
			JsonObject json = new JsonObject();
			json.put("zip",js);
			System.out.println(json);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Original data = ["+originalStr+"]");
		System.out.println("GZipped=["+ Hex.encodeHexString( zippedData )+"]");
		
		
		
		
	}
	
	
	public String compress(String str) throws IOException {
	    if (str == null || str.length() == 0) {
	        return str;
	    }
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    GZIPOutputStream gzip = new GZIPOutputStream(out);
	    gzip.write(str.getBytes());
	    gzip.close();
	    String outStr = out.toString("UTF-8");
	    return outStr;
	 }
	
	public byte[] zipped(final String str) throws IOException {
		  ByteArrayOutputStream byteStream=new ByteArrayOutputStream();
		  Base64OutputStream base64OutputStream=new Base64OutputStream(byteStream);
		  GZIPOutputStream gzip=new GZIPOutputStream(base64OutputStream);
		  OutputStreamWriter writer=new OutputStreamWriter(gzip);
		  Gson gson = new Gson();
		  gson.toJson(str,writer);
		  writer.flush();
		  gzip.finish();
		  writer.close();
		  return byteStream.toByteArray();
		}
	
	public static String compressAndEncodeString(String str) {
	    DeflaterOutputStream def = null;
	    String compressed = null;
	    try {
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        // create deflater without header
	        def = new DeflaterOutputStream(out, new Deflater(Deflater.BEST_COMPRESSION, true));
	        def.write(str.getBytes());
	        def.close();
	        compressed = out.toString("UTF-8");
	    } catch(Exception e) {
	       System.out.println( "could not compress data: " + e);
	    }
	    return compressed;
	}

}
