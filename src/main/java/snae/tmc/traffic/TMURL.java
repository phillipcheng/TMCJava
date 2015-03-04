package snae.tmc.traffic;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TMURL {

	private static Logger logger = LogManager.getLogger(TMURLManager.class);
	
	URL url;
	Proxy proxy = null;
	
	public TMURL(URL url, Proxy proxy){
		this.url = url;
		this.proxy = proxy;
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
			return con;
		}
	}
	
	public HttpsURLConnection getHttpsUrlConnection() throws IOException{
		if (proxy==null){
			return (HttpsURLConnection) url.openConnection();
		}else{
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection(proxy);
			return con;
		}
	}
}
