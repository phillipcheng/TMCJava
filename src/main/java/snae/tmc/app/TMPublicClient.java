package snae.tmc.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.provider.FormEncodingProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import snae.tmc.app.test.AppTest;

import com.vol.common.tenant.Promotion;
import com.vol.common.user.Bonus;
import com.vol.common.user.Quota;
import com.vol.common.user.User;
import com.vol.rest.result.BunosResult;

public class TMPublicClient {
	private static Logger logger = LogManager.getLogger(TMPublicClient.class);
	
	private final String json = "application/json";
	private final String form = "application/x-www-form-urlencoded";
	
	private WebClient client2;
	
	private String server = "http://52.1.96.115:8080";

	private void initClient(){
		JacksonJsonProvider jsonProvider = new JacksonJsonProvider();
		FormEncodingProvider formProvider = new FormEncodingProvider(true);
		List providers = new ArrayList();
		providers.add(jsonProvider);
		providers.add(formProvider);
		
		// public service 
		client2 = WebClient.create(server+"/vol-appserver/public",providers);
		WebClient.getConfig(client2).getInInterceptors().add(new LoggingInInterceptor());
		WebClient.getConfig(client2).getOutInterceptors().add(new LoggingOutInterceptor());
	}
	
	public TMPublicClient(){
		initClient();
	}
	
	public TMPublicClient(String serverUrl){
		this.server = serverUrl;
		initClient();
	}
	
	/**
	 * @param userName
	 * 
	 */
	public List<Quota> getQuota(int tenantId, String userName) {
		Response restResult;
		client2.reset();
		//check quota
		System.out.println("check quota");
		client2.path("quota/"+tenantId);
		client2.query("userName", userName);
		client2.type(form).accept(json);
		restResult = client2.get();
		GenericType<List<Quota>> quotalistType = new GenericType<List<Quota>>(){};
		return restResult.readEntity(quotalistType);
	}

	/**
	 * @param bonusId 
	 * 
	 */
	public boolean activateBonus(int tenantId, long bonusId) {
		Response restResult;
		client2.reset();
		
		System.out.println("active bonus");
		client2.path("activebonus/"+tenantId);
		client2.type(form).accept(json);
		restResult = client2.post("bonusId="+bonusId);
		boolean b = restResult.readEntity(Boolean.class);
		return b;
	}
	
	/**
	 * @param userName
	 * @return
	 */
	public List<Bonus> getUserBonus(int tenantId, String userName) {
		Response restResult;
		client2.reset();

		System.out.println("check user's bonus");
		//check bonus
		client2.path("bonus/"+tenantId);
		client2.query("userName", userName);
		client2.type(form).accept(json);
		restResult = client2.get();
		 GenericType<List<Bonus>> bonusListType = new GenericType<List<Bonus>>(){};
		List<Bonus> bonuses = restResult.readEntity(bonusListType);
		return bonuses;
	}
	
	/**
	 * @param userName 
	 * 
	 */
	public BunosResult grabBonus(int tenantId, int promotionId, 
			String userName, Map<String, String> userProperties) {
		Response restResult;
		client2.reset();
		//grab
		System.out.println("Grab .....");
		client2.path("getbonus/"+tenantId);
		client2.type(form).accept(json);
		String url = "promotionId="+promotionId+"&userName="+userName;
		//append the userProperties to the url
		restResult = client2.post(url);
		return restResult.readEntity(BunosResult.class);
	}

	/**
	 * @param tenantId
	 */
	public List<Promotion> listPublicPromotion(int tenantId) {
		Response restResult;
		client2.reset();
		System.out.println("list  promotion from public");
		client2.path("promotion/"+tenantId);
		client2.type(form).accept(json);
		restResult = client2.get();
		GenericType<List<Promotion>> promotionListType = new GenericType<List<Promotion>>(){};
		return restResult.readEntity(promotionListType);
	}
	
	/**
	 * @param userId 
	 * 
	 */
	public long checkQuota(int tenantId, String userName) {
		Response restResult;
		client2.reset();
		//check quota
		System.out.println("check quota");
		client2.path("quota/"+tenantId);
		client2.query("userName", userName);
		client2.type(form).accept(json);
		restResult = client2.get();
		GenericType<List<Quota>> quotalistType = new GenericType<List<Quota>>(){};
		try{
			List<Quota> ql = restResult.readEntity(quotalistType);
			if (ql.size()>0){
				Quota q = ql.get(0);
				logger.info("balance for tenant %d user %s is %d:", tenantId, userName, q.getBalance());
				return q.getBalance();
			}else{
				logger.info(String.format("quota not found for tenant %d, user:%s", tenantId, userName));
				return 0;
			}
		}catch(Exception e){
			logger.info("", e);
			return 0;
		}
	}
}
