package com.partner.integration.handler;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import com.partner.integration.email.EmailHandler;

public class EmailWorkItemHandler implements WorkItemHandler {
	
	EmailHandler emailhandler = new EmailHandler();

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		emailhandler.sendEmail("pushToGitHubFailed", null, null, "FAIL FAIL FAIL FAIL");
		
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// TODO Auto-generated method stub
		
	}

}
