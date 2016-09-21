package com.partner.integration.conflictresolution;

import java.util.Properties;

import org.eclipse.jgit.util.StringUtils;
import org.jboss.logging.Logger;

import com.partner.utility.PropertiesLoader;

public class ResolutionStrategyHandler {
	
	private static final Logger log = Logger.getLogger(ResolutionStrategyHandler.class.getName());

	public ResolutionStrategyHandler() {
		// TODO Auto-generated constructor stub
	}

	//True indicated manual resolution required false indicated auto resolve
	public Boolean determineResolutionStrategy() {
		Properties generalProperties = PropertiesLoader.loadPropertiesFile("general.properties");
		String resolutionStrategy = generalProperties.getProperty("resolutionStrategy");
		
		if(StringUtils.isEmptyOrNull(resolutionStrategy)){
			log.error("Variable resolutionStrategy not set in general.properties defaulting to manual");
			return true;
		}
		
		switch (resolutionStrategy) {
		case "manual":
			return true;
		case "auto":
			return false;
		default:
			log.error("Variable resolutionStrategy set in general.properties to unexpected value " + resolutionStrategy + " defaulting to manual.");
			return true;
		}
	}
}
