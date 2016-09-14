package com.partner.utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jboss.logging.Logger;

public class PropertiesLoader {
	private static final Logger log = Logger.getLogger(PropertiesLoader.class.getName());
	
	public static Properties loadPropertiesFile(String name){
		String dataDir = System.getProperty("jboss.server.data.directory"); 
		//Used for testing purposes
		//String dataDir = "/home/nreidelb/projects/partner/bcSyncData";
		InputStream is;
		try {
			is = new FileInputStream(dataDir + name);		
			Properties globalProp = new Properties();  
			globalProp.load(is);
			is.close();
			return globalProp;
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return null;
		
	}

}
