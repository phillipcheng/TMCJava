package snae.tmc.traffic.test;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import snae.tmc.traffic.TMHttpUtil;
import snae.tmc.traffic.TMURL;
import snae.tmc.traffic.TMURLManager;


public class TMURLTest {
	private static Logger logger = LogManager.getLogger(TMURLTest.class);
	
	//private static final String proxyhost="192.168.2.8";
	private static final String proxyhost="52.1.96.115";
	private static final int proxyport=80;
	
	private static String url1= "http://news.sina.com.cn";
	private static String url2= "http://finance.sina.com.cn/";
	
	@Test
	//success
	public void test1(){
		String user="cy2";
		TMURLManager tmUrlMgr = new TMURLManager(proxyhost, proxyport);
		tmUrlMgr.start(user);

		HttpURLConnection con = null;
		InputStream is = null;
		int code;
		try{
			//
			TMURL tmurl1 = tmUrlMgr.getUrl(url1);
			con = tmurl1.getHttpUrlConnection();
			con.setRequestMethod("GET");
	        is = con.getInputStream();
	        code = con.getResponseCode();
			assertTrue(code == TMURLManager.SC_OK);
			TMHttpUtil.getStringFromInputStream(is);
			is.close();
			
			//
			tmurl1 = tmUrlMgr.getUrl(url2);
			con = tmurl1.getHttpUrlConnection();
			con.setRequestMethod("GET");
			is = con.getInputStream();
			TMHttpUtil.getStringFromInputStream(is);
	        code = con.getResponseCode();
			assertTrue(code == TMURLManager.SC_OK);
		}catch(Exception e){
			logger.error("", e);
		}
		tmUrlMgr.end();
	}
	
	@Test
	//failed
	public void test2(){
		TMURLManager tmUrlMgr = new TMURLManager(proxyhost, proxyport);
		HttpURLConnection con = null;
		InputStream is = null;
		int code;
		try{
			TMURL tmurl1 = tmUrlMgr.getUrl(url1);
			con = tmurl1.getHttpUrlConnection();
			con.setRequestMethod("GET");
	        code = con.getResponseCode();
			assertTrue(code == TMURLManager.SC_UNAUTHORIZED);
		}catch(Exception e){
			logger.error("", e);
		}
	}
	
	@Test
	//out of balance
	public void test3(){
		String user="cytoomuch";
		TMURLManager tmUrlMgr = new TMURLManager(proxyhost, proxyport);
		tmUrlMgr.start(user);

		HttpURLConnection con = null;
		InputStream is = null;
		int code;
		try{
			while (true){
				//
				TMURL tmurl1 = tmUrlMgr.getUrl(url1);
				con = tmurl1.getHttpUrlConnection();
				con.setRequestMethod("GET");
		        code = con.getResponseCode();
				if (code == TMURLManager.SC_UNAUTHORIZED){
			        is = con.getInputStream();
					String str = TMHttpUtil.getStringFromInputStream(is);
					logger.info(String.format("get content of %d length for url:%s.", str.length(), url1));
					is.close();
				}else{
					logger.info(String.format("get not ok status code %d for url %s.", code, url1));
					break;
				}
			}
		}catch(Exception e){
			logger.error("", e);
		}
		tmUrlMgr.end();
	}
	
	@Test
	//client use multi-thread to use the same session, test server session lock
	public void test4(){
		String user="cyconcurrent";
		TMURLManager tmUrlMgr = new TMURLManager(proxyhost, proxyport);
		tmUrlMgr.start(user);
		String[] urls = new String[]{"http://news.sina.com.cn", "http://www.cnn.com"};
		int numThreads=4;
		List<Thread> runningThreads = new ArrayList<Thread>();
		for (int i=0; i<=numThreads; i++){
			TMHttpClientThread ct = new TMHttpClientThread(i, tmUrlMgr, urls);
			ct.start();
			runningThreads.add(ct);
		}
		for (Thread thread: runningThreads){
			try {
				thread.join();
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
		tmUrlMgr.end();
	}
}
