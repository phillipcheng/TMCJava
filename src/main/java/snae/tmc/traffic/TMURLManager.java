package snae.tmc.traffic;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TMURLManager {
	private static Logger logger = LogManager.getLogger(TMURLManager.class);
	
	private static final String START_URL="http://www.google.com";//any url
	
	//http req custom headers
	private static String HEADER_CMD = "command";
	private static String HEADER_CMDVAL_START = "start";
	private static String HEADER_CMDVAL_STOP = "stop";
	
	private static String HEADER_USERID = "userid";
	private static String HEADER_TENANTID= "tenantid";
	
	//http rsp custom headers
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

	private Proxy proxy;
	private String failedReason = REASON_VAL_UNKNOWN;
	
	
	public static int STATUS_DISCONNECTED=0;
	public static int STATUS_CONNECTING=1;
	public static int STATUS_CONNECTED=2;
	public static int STATUS_DISCONNECTING=3;
	public static int STATUS_ERROR=4;
	private int status;
	
	public static final int SC_UNAUTHORIZED = 401;
	public static final int SC_OK = 200;

	public TMURLManager(String proxyIp, int proxyPort){
		proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIp, proxyPort));
	}
	
	public boolean start(String userId, String tenantId){
		setStatus(STATUS_CONNECTING);
		HttpURLConnection con = null;
		InputStream is = null;
		try {
			URL url = new URL(START_URL);
			con = (HttpURLConnection) url.openConnection(proxy);
			con.setRequestProperty(HEADER_CMD, HEADER_CMDVAL_START);
			con.setRequestProperty(HEADER_USERID, userId);
			con.setRequestProperty(HEADER_TENANTID, tenantId);
			con.setRequestMethod("GET");
	        is = con.getInputStream();
	        int code = con.getResponseCode();
	        
            if (code == SC_OK) {
            	logger.info(String.format("start ok: status code %d.", code));
        		setStatus(STATUS_ERROR);
        		return false;
            }else{
            	String reasonVal = con.getHeaderField(HEADER_REASON);
            	if (reasonVal!=null){
            		logger.error(String.format("status code %d, rejected reason:%s", code, reasonVal));
            		setFailedReason("error:" + reasonVal);
            		setStatus(STATUS_ERROR);
            		return false;
            	}else{
            		logger.error(String.format("no rejected reason found. error status code %s:", code));
            		setFailedReason("unexpected status code:" + code);
            		setStatus(STATUS_ERROR);
            		return false;
            	}
            }
		}catch(Exception e){
			logger.error("",e);
			setFailedReason(e.toString());
    		setStatus(STATUS_ERROR);
			return false;
		}finally{
			if (is!=null){
				try{
					is.close();
				}catch(Exception e){
					logger.error("",e);
				}
			}
		}
	}
	
	public boolean end(){
		setStatus(STATUS_DISCONNECTING);
		HttpURLConnection con = null;
		InputStream is = null;
		try {
			URL url = new URL(START_URL);
			con = (HttpURLConnection) url.openConnection(proxy);
			con.setRequestProperty(HEADER_CMD, HEADER_CMDVAL_STOP);
			con.setRequestMethod("GET");
	        is = con.getInputStream();
	        int code = con.getResponseCode();
            if (code == SC_OK) {
            	logger.info(String.format("end ok: status code %d.", code));
                setStatus(STATUS_DISCONNECTED);
                return true;
            }else{
            	String reasonVal = con.getHeaderField(HEADER_REASON);
            	if (reasonVal!=null){
            		logger.error(String.format("status code %d, rejected reason:%s.", code, reasonVal));
            		setFailedReason("error:" + reasonVal);
            		setStatus(STATUS_ERROR);
            		return false;
            	}else{
            		logger.error(String.format("error status code %s:", code));
            		setFailedReason("unexpected status code:" + code);
            		setStatus(STATUS_ERROR);
            		return false;
            	}
            }
		}catch(Exception e){
			logger.error("",e);
			setFailedReason(e.toString());
    		setStatus(STATUS_ERROR);
			return false;
		}finally{
			if (is!=null){
				try{
					is.close();
				}catch(Exception e){
					logger.error("", e);
				}
			}
		}
	}
	
	public TMURL getUrl(String strUrl) throws MalformedURLException{
		URL url = new URL(strUrl);
		return new TMURL(url, proxy);
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getFailedReason() {
		return failedReason;
	}

	public void setFailedReason(String failedReason) {
		this.failedReason = failedReason;
	}
}
