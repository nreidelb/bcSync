package com.partner.integration.handler;

import java.util.HashMap;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import com.partner.integration.email.EmailHandler;

public class ErrorEmailWorkItemHandler implements WorkItemHandler {
	
	EmailHandler emailhandler = new EmailHandler();

	
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String fileTemplateName = (String) workItem.getParameter("fileTemplateName");
		String toAddress = (String) workItem.getParameter("toAddress");
		String userContact = (String) workItem.getParameter("userContact");
		String errorMessage = (String) workItem.getParameter("errorMessage");
		emailhandler.sendEmail(fileTemplateName, toAddress, userContact, errorMessage);
		manager.completeWorkItem(workItem.getId(), new HashMap<String,Object>());
		
	}

	
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// TODO Auto-generated method stub
		
	}

}
