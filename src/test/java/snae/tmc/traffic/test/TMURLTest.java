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
	
	//private static final String proxyhost="192.168.2.7";
	private static final String proxyhost="52.1.96.115";
	private static final int proxyport=8080;
	
	private static String url1= "http://www.ebay.com/";
	private static String url2= "https://www.yahoo.com/";
	
	@Test
	public void testStopSession(){
		TMURLManager tmUrlMgr = new TMURLManager(proxyhost, proxyport);
		tmUrlMgr.end();
	}
	
	@Test
	//success
	public void test1(){
		String user="1234";
		String tenantId = "3";
		TMURLManager tmUrlMgr = new TMURLManager(proxyhost, proxyport);
		tmUrlMgr.start(user, tenantId);
		try{
			for (int i=0;i<2;i++){
				//
				//TMHttpUtil.getContentFromHttpGetURL(tmUrlMgr, url1);
				TMHttpUtil.getContentFromHttpsGetURL(tmUrlMgr, url2);
				//TMHttpUtil.getContentFromHttpGetURL(tmUrlMgr, url1);
				TMHttpUtil.getContentFromHttpsGetURL(tmUrlMgr, url2);
			}
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
		String user="1234";
		String tenantId = "3";
		TMURLManager tmUrlMgr = new TMURLManager(proxyhost, proxyport);
		tmUrlMgr.start(user, tenantId);
		
		try{
			while (true){
				//
				TMHttpUtil.getContentFromHttpGetURL(tmUrlMgr, url1);
			}
		}catch(Exception e){
			logger.error("", e);
		}
		tmUrlMgr.end();
	}
	
	@Test
	//client use multi-thread to use the same session, test server session lock
	public void test4(){
		String user="abc";
		String tenantId = "3";
		TMURLManager tmUrlMgr = new TMURLManager(proxyhost, proxyport);
		tmUrlMgr.start(user, tenantId);
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
