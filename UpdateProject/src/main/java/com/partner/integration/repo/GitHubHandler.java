package com.partner.integration.repo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.jboss.logging.Logger;

import com.partner.utility.PropertiesLoader;

public class GitHubHandler {
	private static final Logger log = Logger.getLogger(GitHubHandler.class.getName());
	
	public void createPullRequest(String title, String message, String branch){
		Properties gitHubProperties = PropertiesLoader.loadPropertiesFile("github.properties");
		String url = gitHubProperties.getProperty("pullRequestUrl");
		URL obj;
		try {
			obj = new URL(url);
			
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
			
			con.setRequestMethod("POST");
			String authString = gitHubProperties.getProperty("userName") + ":" + gitHubProperties.getProperty("password");			
			con.setRequestProperty("Authorization", "Basic " + new String(Base64.encodeBase64(authString.getBytes())));
			con.setRequestProperty("Content-Type","application/json");
	
			String rawBody = "{"
					+ "\"title\": \""+title+"\", "
					+ "\"body\": \""+message+"\", "
					+ "\"head\": \""+branch+"\", "
					+ "\"base\": \"master\"}";
	
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(rawBody);
			wr.flush();
			wr.close();
	
			int responseCode = con.getResponseCode();
			log.debug("Sending 'POST' request to URL : " + url);
			log.debug("Body : " + rawBody);
			log.debug("Response Code : " + responseCode);
	
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			//print result
			log.info("Pull request response: " + response.toString());
			
		} catch (MalformedURLException e) {
			log.error(e.getMessage());
		} catch (ProtocolException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}


	}

}
