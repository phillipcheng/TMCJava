/**
 * 
 */
package snae.tmc.app.test;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.provider.FormEncodingProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.junit.Test;

import snae.tmc.app.TMAdminClient;
import snae.tmc.app.TMPublicClient;
import snae.tmc.traffic.test.TMHttpClientTest;

import com.vol.common.tenant.Operator;
import com.vol.common.tenant.Promotion;
import com.vol.common.tenant.PromotionBalance;
import com.vol.common.tenant.Tenant;
import com.vol.common.user.Bonus;
import com.vol.common.user.Quota;
import com.vol.common.user.User;
import com.vol.rest.result.BunosResult;
import com.vol.rest.result.OperationResult;
import com.vol.rest.result.PutOperationResult;

/**
 * @author scott
 *
 */
public class AppTest {
	private static Logger logger = LogManager.getLogger(AppTest.class);
	
	//private final String server = "http://52.1.96.115:8080";
	private final String server = "http://127.0.0.1:8080";
	private final long bonusSize = 5*1024*1024;
	private final long promotionMaxSize = 20*1024*1024;
	
	public int createAndActivatePromotion(int tenantId, int operatorId){
		TMAdminClient adminClient = new TMAdminClient(server);
		
		Promotion promotion = new Promotion();
		promotion.setBonusExpirationTime(System.currentTimeMillis()+365*24*60*60*1000);
		promotion.setStartTime(System.currentTimeMillis());
		promotion.setEndTime(System.currentTimeMillis()+365*24*60*60*1000);
		promotion.setDescription("Promotion Test1");
		promotion.setName("Promotion1");
		promotion.setMaximum(promotionMaxSize);
		promotion.setRule(String.format("return %d;", bonusSize));

		promotion.setTenantId(tenantId);
		promotion.setLastUpdateOperator(operatorId);
		int promotionId = adminClient.createPromotion(promotion);
		adminClient.getPromotion(tenantId, promotionId);	
		adminClient.activePromotion(promotionId);
		
		return promotionId;
	}
	
	public void grabAndActivateBonus(int tenantId, int promotionId, String userName){
		TMPublicClient publicClient = new TMPublicClient(server);
		TMAdminClient adminClient = new TMAdminClient(server);
		
		List<Promotion> plist = publicClient.listPublicPromotion(tenantId);
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
		}
	}
	
	@Test
	public void test1(){
		TMAdminClient adminClient = new TMAdminClient(server);
		Tenant tanent = new Tenant();
		tanent.setName("TenantAli");
		tanent.setDescription("Ali Tenant");
		int tenantId = adminClient.createTenant(tanent);
		//
		Operator operator = new Operator();
		operator.setName("operatorAli1");
		operator.setPassword("operatorAli1");
		operator.setTenantId(tenantId);
		int operatorId = adminClient.createOperator(operator);
		//
		
		createAndActivatePromotion(tenantId, operatorId);
	}
	
	@Test
	public void CAAPromotion(){
		int tenantId=3;
		int operatorId=3;
		createAndActivatePromotion(tenantId,operatorId);
	}
	
	@Test
	public void GAABonus(){
		int tenantId = 3;
		int promotionId = 4;
		String userName = "abc";
		this.grabAndActivateBonus(tenantId, promotionId, userName);
	}
}
