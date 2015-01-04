import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.att.m2x.client.M2XClient;
import com.att.m2x.client.M2XDevice;
import com.att.m2x.client.M2XStream;

public class FetchPollenData {
	private static final String POLLEN_URL = "http://api-m2x.att.com/v2/devices/e0664612675c3f92e43ab42c1c433979/streams/pollen";
	private static final String USER_AGENT = "Mozilla/5.0";
	private static final String M2X_HEADER = "H-M2X-KEY";
	private static final String API_KEY = "b4eab554dc00784b372646e93c7bd0b4";
	private static M2XClient client = new M2XClient("6192f89a79f3c9f2d006b78eda4df0cc");
	private static M2XDevice device = client.device("15af65895ed19be59193b8b303b76a8f");
	private static M2XStream stream = device.stream("pollen");
	
	public static void main(String[] args) throws Exception{
		
		//createStream();
		int count = 0;
		while(count < 1000)
		{
			final Double pd = Double.parseDouble(findPollenData());
			stream.updateValue(M2XClient.jsonSerialize(new HashMap<String, Object>()
				    {{
				        put("value", pd);
				    }}));
			Thread.sleep(30000);
			count++;
		}
	}
	
	private static void createStream() throws IOException
	{
		M2XStream stream = device.stream("pollen");
	    stream.createOrUpdate("{\"type\":\"numeric\",\"unit\":{\"label\":\"Level\",\"symbol\":\"pt\"}}");

	}
	private static String findPollenData() {
		String retValue = null;
		
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(POLLEN_URL);
	 
			// add request header
			request.addHeader("User-Agent", USER_AGENT);
			request.addHeader(M2X_HEADER, API_KEY);
	 
			HttpResponse response = client.execute(request);
	 
			System.out.println("\nSending 'GET' request to URL : " + POLLEN_URL);
			System.out.println("Response Code : " + 
	                       response.getStatusLine().getStatusCode());
	 
			BufferedReader rd = new BufferedReader(
	                       new InputStreamReader(response.getEntity().getContent()));
	 
			String line = "";
			while ((line = rd.readLine()) != null) {
				int ind = line.indexOf("\"value\"");
				int ind2 = line.indexOf(',', ind);
				retValue = line.substring(ind+8, ind2);
				System.out.println(retValue);
			}

		} catch (Exception ex) {
			for(StackTraceElement elem : ex.getStackTrace())
			{
				System.out.println(elem.toString());
			}
			System.out.println("found exception: " + ex.getMessage());
			
		}
		return retValue;
	}
}
