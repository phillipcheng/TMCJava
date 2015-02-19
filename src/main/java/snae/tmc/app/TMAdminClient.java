package snae.tmc.app;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.provider.FormEncodingProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import com.vol.common.tenant.Operator;
import com.vol.common.tenant.Promotion;
import com.vol.common.tenant.PromotionBalance;
import com.vol.common.tenant.Tenant;
import com.vol.rest.result.OperationResult;
import com.vol.rest.result.PutOperationResult;

public class TMAdminClient {
	private final String json = "application/json";
	private final String form = "application/x-www-form-urlencoded";
	private WebClient client;
	
	private String server = "http://52.1.96.115:8080";

	private void initClient(){
		JacksonJsonProvider jsonProvider = new JacksonJsonProvider();
		FormEncodingProvider formProvider = new FormEncodingProvider(true);
		List providers = new ArrayList();
		providers.add(jsonProvider);
		providers.add(formProvider);
		
		client = WebClient.create(server+"/vol-appserver/admin",providers);
		WebClient.getConfig(client).getInInterceptors().add(new LoggingInInterceptor());
		WebClient.getConfig(client).getOutInterceptors().add(new LoggingOutInterceptor()); 
	}
	
	public TMAdminClient(){
		initClient();
	}
	
	public TMAdminClient(String serverUrl){
		this.server = serverUrl;
		initClient();
	}
	
	/**
	 * @param promotionId 
	 * 
	 */
	public PromotionBalance checkPromotionbalance(int promotionId) {
		Response restResult;
		//check promotion balance
		client.reset();

		System.out.println("check promotionbalance");
		client.path("promotionbalance");
		client.type(json).accept(json);
		client.query("promotionId", promotionId);
		restResult = client.get();
		return restResult.readEntity(PromotionBalance.class);
	}

	/**
	 * @param promotionId 
	 */
	public void activePromotion(int promotionId) {
		Response restResult;
		client.reset();
		
		System.out.println("active  promotion");
		//active promotion
		client.path("promotion/active/"+promotionId);
		client.type(json).accept(json);
		restResult = client.post(null);
		OperationResult result4 = restResult.readEntity(OperationResult.class);
		System.out.println("active promotion:"+result4);
	}

	/**
	 * @param promotionId 
	 */
	public Promotion getPromotion(int tenantId, int promotionId) {
		Response restResult;
		client.reset();
		
		System.out.println("get  promotion");

		client.path("promotion/"+tenantId+"/"+promotionId);
		client.type(json).accept(json);
		restResult = client.get();
		return restResult.readEntity(Promotion.class);
	}

	/**
	 * @param promotion
	 * @return
	 */
	public int createPromotion(Promotion promotion) {
		Response restResult;
		client.reset();
		//create promotion
		System.out.println("creating promotion");
		client.path("promotion");
		client.type(json).accept(json);
		
		restResult = client.put(promotion);
		PutOperationResult result3 = restResult.readEntity(PutOperationResult.class);
		System.out.println("put promotion "+result3);
		return (int) result3.getId();
	}

	/**
	 * @param operatorid 
	 */
	public Operator getOperator(int operatorid) {
		Response restResult;
		client.reset();
		System.out.println("Check Operator");
		client.path("operator/"+operatorid);
		restResult = client.get();
		return restResult.readEntity(Operator.class);
	}

	/**
	 *
	 * @param operator
	 */
	public int createOperator(Operator operator) {
		Response restResult;
		client.reset();
		
		// create operator
		System.out.println("creating Operator");
		client.path("operator");
		client.type(json).accept(json);
		
		restResult = client.put(operator);
		PutOperationResult result2 = restResult.readEntity(PutOperationResult.class);
		System.out.println("put operator "+result2);
		return (int) result2.getId();
	}

	/**
	 * @param tanent
	 * @return
	 */
	public int createTenant(Tenant tanent) {
		Response restResult;
		client.reset();
		// create tenant
		System.out.println("creating Tenant");
		client.path("tenant");
		client.type(json).accept(json);
		
		restResult = client.put(tanent);
		PutOperationResult result = restResult.readEntity(PutOperationResult.class);
		System.out.println("put tenant "+result);

		return (int) result.getId();
	}

	/**
	 * @param tenantId
	 */
	public Tenant getTenant(int tenantId) {
		Response restResult;
		client.reset();
		System.out.println("check Tenant");

		client.path("tenant/"+tenantId);
		restResult = client.get();
		return restResult.readEntity(Tenant.class);
	}

}
