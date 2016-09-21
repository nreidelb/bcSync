package com.partner.integration.git;

import java.util.Properties;

import org.eclipse.jgit.util.StringUtils;
import org.jboss.logging.Logger;

import com.partner.utility.PropertiesLoader;

public class PushPermissionHandler {
	
	private static final Logger log = Logger.getLogger(PushPermissionHandler.class.getName());

	public PushPermissionHandler() {
	}

	public Boolean checkPushPermission() {
		Properties generalProperties = PropertiesLoader.loadPropertiesFile("general.properties");
		String globalBaPushPermission = generalProperties.getProperty("baPushPermission");
		
		if(StringUtils.isEmptyOrNull(globalBaPushPermission)){
			log.error("Variable globalBaPushPermission not set in general.properties defaulting to false");			
		}
		return Boolean.valueOf(globalBaPushPermission);
	}
}
