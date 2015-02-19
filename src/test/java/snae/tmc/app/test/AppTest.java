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
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.junit.Test;

import snae.tmc.app.TMAdminClient;
import snae.tmc.app.TMPublicClient;

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
	
	private final String server = "http://52.1.96.115:8080";
	private final long bonusSize = 5*1024*1024;
	
	public int createAndActivatePromotion(int tenantId, int operatorId){
		TMAdminClient adminClient = new TMAdminClient(server);
		
		Promotion promotion = new Promotion();
		promotion.setBonusExpirationTime(System.currentTimeMillis()+365*24*60*60*1000);
		promotion.setStartTime(System.currentTimeMillis());
		promotion.setEndTime(System.currentTimeMillis()+365*24*60*60*1000);
		promotion.setDescription("Promotion Test1");
		promotion.setName("Promotion1");
		promotion.setMaximum(10);
		promotion.setRule(String.format("return %l;", bonusSize));
		

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
		
		BunosResult bonusResult = publicClient.grabBonus(tenantId, promotionId, userName, null);	

		pb = adminClient.checkPromotionbalance(promotionId);
		long nbalance = pb.getBalance();
		//assert nbalance - obalance = bonusSize
		
		publicClient.activateBonus(tenantId, bonusResult.getBonus().getId());
		
		publicClient.checkQuota(tenantId, userName);
	}
	
	@Test
	public void test1(){
		TMAdminClient adminClient = new TMAdminClient(server);
		Tenant tanent = new Tenant();
		tanent.setName("Tenant1");
		tanent.setDescription("first Tenant");
		int tenantId = adminClient.createTenant(tanent);
		//
		Operator operator = new Operator();
		operator.setName("operator1");
		operator.setPassword("passwor22d");
		operator.setTenantId(tenantId);
		int operatorId = adminClient.createOperator(operator);
		//
		
	}
}
