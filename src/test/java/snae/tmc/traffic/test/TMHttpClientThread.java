package snae.tmc.traffic.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import snae.tmc.traffic.TMHttpUtil;
import snae.tmc.traffic.TMURL;
import snae.tmc.traffic.TMURLManager;

public class TMHttpClientThread extends Thread {
	private static Logger logger = LogManager.getLogger(TMHttpClientThread.class);
	
	//urls to get
	String[] urls;
	TMURLManager tmMgr;
	int threadId;
	
	public TMHttpClientThread(int threadId, TMURLManager tmMgr, String[] urls){
		this.tmMgr = tmMgr;
		this.urls = urls;
		this.threadId = threadId;
	}

	@Override
	public void run() {
		for (String url: urls){
			try {
				TMURL tmUrl = tmMgr.getUrl(url);
				HttpURLConnection con = tmUrl.getHttpUrlConnection();
		        int code = con.getResponseCode();
				if (code == HttpURLConnection.HTTP_UNAUTHORIZED){
					logger.info(String.format("thread %d, unauthorized for url:%s.", 
							threadId, url));
					break;
				}else{
					InputStream is = con.getInputStream();
					String str = TMHttpUtil.getStringFromInputStream(is);
					logger.info(String.format("thread %d, content of %d length for url:%s.", 
							threadId, str.length(), url));
					is.close();
				}
			} catch (IOException e) {
				logger.error("", e);
			}
		}
	}
}
