package snae.tmc.traffic;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.cert.Certificate;

public class TMHttpUtil {

	private static Logger logger = LogManager.getLogger(TMHttpUtil.class);
	
	public static String getStringFromInputStream(InputStream in, String encoding) throws IOException{	
		BufferedReader reader = null;
		if (encoding==null){
			reader = new BufferedReader(new InputStreamReader(in));
		}else{
			reader = new BufferedReader(new InputStreamReader(in, encoding));
		}
		String line = null;
		StringBuilder responseData = new StringBuilder();
		while((line = reader.readLine()) != null) {
		    responseData.append(line);
		}
        return responseData.toString();
	}
	
	public static String getStringFromInputStream(InputStream in) throws IOException{		
		return getStringFromInputStream(in, null);
	}
	
	public static String getContentFromHttpGetURL(TMURLManager tmUrlMgr, String url1) throws IOException {
		TMURL tmurl1 = tmUrlMgr.getUrl(url1);
		HttpURLConnection con = tmurl1.getHttpUrlConnection();
		con.setRequestMethod("GET");
        InputStream is = con.getInputStream();
        int code = con.getResponseCode();
		assertTrue(code == TMURLManager.SC_OK);
		String str = TMHttpUtil.getStringFromInputStream(is);
        logger.info(String.format("Content from url:%s is of length:%d", url1, str.length()));
		is.close();
		con.disconnect();
		return str;
	}
	
	public static String getContentFromHttpsGetURL(TMURLManager tmUrlMgr, String url1) throws IOException {
		TMURL tmurl1 = tmUrlMgr.getUrl(url1);
		HttpsURLConnection con = tmurl1.getHttpsUrlConnection();
		con.setRequestMethod("GET");
        int code = con.getResponseCode();
        String str=null;
		if (code == TMURLManager.SC_OK){
			Certificate[] certs = con.getServerCertificates();
			for(Certificate cert : certs){
			   logger.info("Cert Type : " + cert.getType());
			   logger.info("Cert Hash Code : " + cert.hashCode());
			   logger.info("Cert Public Key Algorithm : " 
		                                    + cert.getPublicKey().getAlgorithm());
			   logger.info("Cert Public Key Format : " 
		                                    + cert.getPublicKey().getFormat());
			}
	        InputStream is = con.getInputStream();
	        str = TMHttpUtil.getStringFromInputStream(is);
	        logger.info(String.format("Content from url:%s is of length:%d", url1, str.length()));
			is.close();
			con.disconnect();
		}
		return str;
	}
}
