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
				TMHttpUtil.getContentFromHttpGetURL(tmMgr, url);
			} catch (IOException e) {
				logger.error("", e);
			}
		}
	}
}
