package com.partner.utility;

public class StringParser {
	
	public static String removeUserPassword(String url){
		if(url == null){
			return null;
		}
		if(url.indexOf("@")<0){
			return url;
		}
		return url.substring(url.indexOf("@"), url.length());
	}

}
