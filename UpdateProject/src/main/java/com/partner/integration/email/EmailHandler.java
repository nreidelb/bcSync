package com.partner.integration.email;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.jgit.util.StringUtils;
import org.jboss.logging.Logger;

import com.partner.utility.PropertiesLoader;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;


public class EmailHandler {
	private static final String SENDER_ADDRESS = "senderAddress";

	private static final Logger log = Logger.getLogger(EmailHandler.class.getName());
	
	private static final Properties DEFAULT_EMAIL_PROPERTIES = PropertiesLoader.loadPropertiesFile("email.properties");
	private static final Properties GIT_HUB_PROPERTIES = PropertiesLoader.loadPropertiesFile("github.properties");
	private static final Properties BC_PROPERTIES = PropertiesLoader.loadPropertiesFile("business-central.properties");
	
	
	private Configuration configuration = null;
	
	private void setupCfg(){
		try {		
			configuration = new Configuration();
			configuration.setDirectoryForTemplateLoading(new File(System.getProperty("jboss.server.data.directory") + "freemarker"));
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	public void sendEmail(String templateFileName, String toAddress, String userEmail, String errorDetails){
		try {
			Properties emailProperties = PropertiesLoader.loadPropertiesFile(templateFileName + ".properties");
			Template template = getFreemarkerConfiguration().getTemplate(templateFileName +".ftl");
			
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("gitRepo", GIT_HUB_PROPERTIES.getProperty("repoUrl"));
			data.put("bcRepo", BC_PROPERTIES.getProperty("repoUrl"));
			data.put("userEmail", defaultIfNull(userEmail,DEFAULT_EMAIL_PROPERTIES.getProperty("defaultUserEmail")));
			data.put("errorMessage",errorDetails);
            
			StringWriter bodyWriter = new StringWriter();
			template.process(data, bodyWriter);
			InitialContext ctx = new InitialContext();  
			Session mailSession = (Session)ctx.lookup("java:jboss/mail/Default");
			log.info(mailSession);
			MimeMessage m = new MimeMessage(mailSession);
            Address from = new InternetAddress(emailProperties.getProperty(SENDER_ADDRESS, DEFAULT_EMAIL_PROPERTIES.getProperty(SENDER_ADDRESS)));
            Address[] to = new InternetAddress[] {new InternetAddress(defaultIfNull(toAddress, emailProperties.getProperty("errorHandler", DEFAULT_EMAIL_PROPERTIES.getProperty("errorHandler")))) };

            m.setFrom(from);
            m.setRecipients(Message.RecipientType.TO, to);
            m.setSubject(emailProperties.getProperty("subject"));
            m.setSentDate(new java.util.Date());
            m.setContent(bodyWriter.toString(),"text/plain");
            Transport.send(m);
		} catch (IOException e) {
			log.error(e.getMessage());
		} catch (MessagingException e) {
			log.error(e.getMessage());
		} catch (TemplateException e) {
			log.error(e.getMessage());
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	private String defaultIfNull(String value, String defaultValue) {
		return StringUtils.isEmptyOrNull(value) ? defaultValue : value;
	}
	
	private Configuration getFreemarkerConfiguration(){
		if(configuration == null){
			setupCfg();
		}
		return configuration;
	}
	

}
