package com.partner.integration.handler;

import java.util.HashMap;

import org.eclipse.jgit.util.StringUtils;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import com.partner.integration.git.GitHandler;

public class RemoteToBusinessCentralWorkItemHandler implements WorkItemHandler{
	
	GitHandler gitHandler = new  GitHandler();

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String failureMessage = gitHandler.updateBusinessCentralRepo();
		Boolean success = StringUtils.isEmptyOrNull(failureMessage);
		
		HashMap<String, Object> results = new HashMap<String,Object>();
		results.put("pushSuccess", success);
		results.put("failureMessage", failureMessage);
		manager.completeWorkItem(workItem.getId(), results);
		
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		
	}

}
