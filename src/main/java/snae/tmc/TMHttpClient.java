package snae.tmc;

import java.io.IOException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TMHttpClient extends HttpClient{
	private static Logger logger = LogManager.getLogger(TMHttpClient.class);
	
	private static final String START_URL="http://www.google.com";//any url
	private static String HEADER_CMD = "command";
	private static String HEADER_CMDVAL_START = "start";
	private static String HEADER_CMDVAL_STOP = "stop";
	private static String HEADER_SESSIONID = "dsessionid";
	private static String HEADER_USERID = "userid";
	private static String HEADER_REASON = "rejectreason";
	
	private static String REASON_VAL_SUCCESS="command succeed";
	private static String REASON_VAL_NOREQHEAD="no req head";
	private static String REASON_VAL_REQHEAD_NOUSERIP="no user/ip in the start request header";
	private static String REASON_VAL_NOUSER="no such user";
	private static String REASON_VAL_NOBAL="no balance";
	private static String REASON_VAL_NOIPSESSION="no ip session";
	private static String REASON_VAL_USERONLINE="user already online";
	private static String REASON_VAL_IPINUSE="ip already in use";
	private static String REASON_VAL_NORSPHEAD="no rsp head";
	private static String REASON_VAL_UNKNOWN="unknown";

	private String userId;
	private String userSessionId;
	private String failedReason = REASON_VAL_UNKNOWN;

	private static HttpConnectionManager httpclientMgr = new MultiThreadedHttpConnectionManager();
	
	public TMHttpClient(String userId, String proxyHost, int proxyPort){
		super(httpclientMgr);
		this.userId = userId;
		HostConfiguration config = getHostConfiguration();
		config.setProxy(proxyHost, proxyPort);
	}
	
	public TMHttpClient(String userId){
		super(httpclientMgr);
		this.userId = userId;
		//get the proxyHost and port from proxy-selector-server
	}
	
	public boolean start(){
		HttpMethod method = new GetMethod(START_URL);
		try {
			method.setRequestHeader(HEADER_CMD, HEADER_CMDVAL_START);
			method.setRequestHeader(HEADER_USERID, userId);
	        super.executeMethod(method);
            if (method.getStatusCode() == HttpStatus.SC_OK) {
            	Header header = method.getResponseHeader(HEADER_SESSIONID);
            	if (header!=null){
                    logger.info(String.format("start ok. session id got:%s", header.getValue()));
                    userSessionId = header.getValue();
                    return true;
            	}else{
            		return false;
            	}
            }else{
            	Header header = method.getResponseHeader(HEADER_REASON);
            	if (header!=null){
            		logger.error(String.format("status code %d, rejected reason:%s", method.getStatusCode(), header.getValue()));
            		failedReason = header.getValue();
            		return false;
            	}else{
            		logger.error(String.format("no rejected reason found. error status code %s:", method.getStatusCode()));
            		return false;
            	}
            }
		}catch(Exception e){
			logger.error("",e);
			return false;
		}finally{
			method.releaseConnection();
		}
	}
	
	public boolean end(){
		HttpMethod method = new GetMethod(START_URL);
		try {
			method.setRequestHeader(HEADER_CMD, HEADER_CMDVAL_STOP);
			method.setRequestHeader(HEADER_SESSIONID, userSessionId);
	        super.executeMethod(method);
            if (method.getStatusCode() == HttpStatus.SC_OK) {
                String result = method.getResponseBodyAsString();
                logger.info(String.format("start ok. result body:%s", result));
                return true;
            }else{
            	Header header = method.getResponseHeader(HEADER_REASON);
            	if (header!=null){
            		logger.error(String.format("status code %d, rejected reason:%s", method.getStatusCode(), header.getValue()));
            		failedReason = header.getValue();
            		return false;
            	}else{
            		logger.error(String.format("error status code %s:", method.getStatusCode()));
            		failedReason = REASON_VAL_NORSPHEAD;
            		return false;
            	}
            }
		}catch(Exception e){
			logger.error("",e);
			failedReason = REASON_VAL_UNKNOWN;
			return false;
		}finally{
			method.releaseConnection();
		}
	}
	
	@Override
	public int executeMethod(HttpMethod method)
	        throws IOException, HttpException  {
		method.setRequestHeader(HEADER_SESSIONID, userSessionId);
		return super.executeMethod(method);
	}
	
	public String getFailedReason() {
		return failedReason;
	}

	public void setFailedReason(String failedReason) {
		this.failedReason = failedReason;
	}
	
}
