package snae.tmc.traffic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TMHttpUtil {
	public static String getStringFromInputStream(InputStream in, String encoding) throws IOException{	
		BufferedReader reader = null;
		if (encoding==null){
			reader = new BufferedReader(new InputStreamReader(in));
		}else{
			reader = new BufferedReader(new InputStreamReader(in, encoding));
		}
		String line = null;
		StringBuilder responseData = new StringBuilder();
		while((line = reader.readLine()) != null) {
		    responseData.append(line);
		}
        return responseData.toString();
	}
	
	public static String getStringFromInputStream(InputStream in) throws IOException{		
		return getStringFromInputStream(in, null);
	}
}
