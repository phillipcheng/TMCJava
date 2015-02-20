package snae.tmc.test;

import java.net.HttpURLConnection;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.vol.common.tenant.Promotion;
import com.vol.common.tenant.PromotionBalance;
import com.vol.common.user.Bonus;
import com.vol.rest.result.BunosResult;

import snae.tmc.app.TMAdminClient;
import snae.tmc.app.TMPublicClient;
import snae.tmc.traffic.TMHttpClient;

public class IntegrationTest {
	private static Logger logger = LogManager.getLogger(IntegrationTest.class);
	
	private static final String appServerUrl = "http://52.1.96.115:8080";	
	private static final String proxyhost="52.1.96.115";
	private static final int proxyport=80;
	private static final long bonusSize = 5*1024*1024;
	private static String url1= "http://news.sina.com.cn";
	
	private TMPublicClient publicClient = null;
	private TMAdminClient adminClient = null;
	private TMHttpClient tmhttpclient = null;
	
	public IntegrationTest(){
		publicClient = new TMPublicClient(appServerUrl);
		adminClient = new TMAdminClient(appServerUrl);
	}
	
	public Bonus grabAndActivateBonus(int tenantId, int promotionId, String userName){
		//assert plist contains the promotionid
		PromotionBalance pb = adminClient.checkPromotionbalance(promotionId);
		long obalance = pb.getBalance();
		long qobalance = publicClient.checkQuota(tenantId, userName);
		logger.info(String.format("balance of promotion %d is %d", promotionId, obalance));
		logger.info(String.format("balance of tenant %d, user %s is %d", tenantId, userName, qobalance));
		
		BunosResult bonusResult = publicClient.grabBonus(tenantId, promotionId, userName, null);

		logger.info(String.format("result for grabBonus: %d", bonusResult.getCode()));
		if (bonusResult.getBonus()!=null){
			pb = adminClient.checkPromotionbalance(promotionId);
			long nbalance = pb.getBalance();
			publicClient.activateBonus(tenantId, bonusResult.getBonus().getId());
			long qnbalance = publicClient.checkQuota(tenantId, userName);
			
			logger.info(String.format("balance of promotion %d is %d, gap is %d", 
					promotionId, nbalance, nbalance-obalance));
			logger.info(String.format("balance of tenant %d, user %s is %d, gap is %d", 
					tenantId, userName, qnbalance, qnbalance-qobalance));
			return bonusResult.getBonus();
		}else
			return null;
	}
	
	@Test
	public void test1(){
		int tenantId = 3;
		int promotionId = 4;
		String userName = "abc";
		
		if (adminClient.getTenant(tenantId)==null){
			logger.info(String.format("tenant not found for id:%d", tenantId));
			return;
		}
		Promotion p = adminClient.getPromotion(tenantId, promotionId);
		if (p==null){
			logger.info(String.format("promotion not found for id:%d, tenant id:%d", promotionId, tenantId));
			return;
		}
		PromotionBalance pb = adminClient.checkPromotionbalance(promotionId);
		if (pb==null){
			logger.error(String.format("promotion %d found but its balance not found.", promotionId));
			return;
		}
		
		if (pb.getBalance() < bonusSize){
			logger.info(String.format("Not enough balance %d left in promotion %d for requested bonus size %d", 
					pb.getBalance(), promotionId, bonusSize));
			return;
		}
			
		Bonus bonus = grabAndActivateBonus(tenantId, promotionId, userName);
		if (bonus == null){
			logger.error("grab bonus failed for tenant:%d, promotion:%d, user:%s", 
					tenantId, promotionId, userName);
			return;
		}
		
		tmhttpclient = new TMHttpClient(userName, tenantId+"", proxyhost, proxyport);
		tmhttpclient.start();
		try{
			while(true){
				//
				GetMethod method = new GetMethod(url1);
				tmhttpclient.executeMethod(method);
				int statusCode = method.getStatusCode();
				int len = method.getResponseBody().length;
				if (statusCode == HttpURLConnection.HTTP_OK){
					logger.info(String.format("status code is %d for getting url:%s, len:%d", 
							statusCode, url1, len));
				}else{
					break;
				}
			}
		}catch(Exception e){
			logger.error("", e);
		}
		tmhttpclient.end();
	}
}
