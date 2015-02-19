package snae.tmc.traffic;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TMURL {

	private static Logger logger = LogManager.getLogger(TMURLManager.class);
	
	URL url;
	Proxy proxy = null;
	String sessionId = null;
	
	public TMURL(URL url, Proxy proxy, String sessionId){
		this.url = url;
		this.proxy = proxy;
		this.sessionId = sessionId;
	}
	
	//for normal access
	public TMURL(String url) throws MalformedURLException{
		this.url = new URL(url);
	}
	
	public URL getUrl(){
		return url;
	}
	
	public HttpURLConnection getHttpUrlConnection() throws IOException{
		if (proxy==null){
			return (HttpURLConnection) url.openConnection();
		}else{
			HttpURLConnection con = (HttpURLConnection) url.openConnection(proxy);
			con.setRequestProperty(TMURLManager.HEADER_SESSIONID, sessionId);
			return con;
		}
	}
}
