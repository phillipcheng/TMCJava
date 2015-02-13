package snae.tmc.test;

import static org.junit.Assert.*;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import snae.tmc.TMHttpClient;

public class TMHttpClientTest {
	private static Logger logger = LogManager.getLogger(TMHttpClientTest.class);
	
	private static final String proxyhost="192.168.2.7";
	//private static final String proxyhost="52.1.96.115";
	private static final int proxyport=8080;
	private static final int SC_Unauthorized=401;
	private static final int SC_Success=200;
	private int statusCode =0;
	
	private static String url1= "http://news.sina.com.cn";
	private static String url2= "http://finance.sina.com.cn/";
	
	
	@Test
	//success
	public void test1(){
		String user="cy2";
		TMHttpClient tmhttpclient = new TMHttpClient(user, proxyhost, proxyport);
		tmhttpclient.start();
		try{
			//
			GetMethod method = new GetMethod(url1);
			tmhttpclient.executeMethod(method);
			statusCode = method.getStatusCode();
			assertTrue(statusCode == SC_Success);
			//
			method = new GetMethod(url2);
			tmhttpclient.executeMethod(method);
			statusCode = method.getStatusCode();
			assertTrue(statusCode == SC_Success);
			logger.info(String.format("status code is %d for getting url:%s", statusCode, url2));
		}catch(Exception e){
			logger.error("", e);
		}
		tmhttpclient.end();
	}
	
	@Test
	//failed
	public void test2(){
		String user="cy2";
		TMHttpClient tmhttpclient = new TMHttpClient(user, proxyhost, proxyport);
		try{
			GetMethod method = new GetMethod(url1);
			tmhttpclient.executeMethod(method);
			
			statusCode = method.getStatusCode();
			assertTrue(statusCode == SC_Unauthorized);
		}catch(Exception e){
			logger.error("", e);
		}
	}
}
