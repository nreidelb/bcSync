package com.partner.utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jboss.logging.Logger;

public class PropertiesLoader {
	private static final String JBOSS_SERVER_DATA_DIRECTORY_PROPERTY = "jboss.server.data.directory";
	private static final Logger log = Logger.getLogger(PropertiesLoader.class.getName());
	
	public static Properties loadPropertiesFile(String name){
		String dataDir = System.getProperty(JBOSS_SERVER_DATA_DIRECTORY_PROPERTY); 
		//Used for testing purposes
		//String dataDir = "/home/nreidelb/projects/partner/bcSyncData/";
		InputStream is;
		try {
			is = new FileInputStream(dataDir + name);		
			Properties globalProp = new Properties();  
			globalProp.load(is);
			is.close();
			if(globalProp.isEmpty()){
				log.error("Could not find file" + name + " at location " + dataDir + name);
			}
			return globalProp;
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		log.error("Could not find file" + name + " at location " + dataDir + name + ". Make sure " + JBOSS_SERVER_DATA_DIRECTORY_PROPERTY + " is set to the correct directory in your standalone.xml" );
		return null;
		
	}

}
