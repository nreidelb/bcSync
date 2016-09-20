package com.partner.integration.handler;

import java.util.HashMap;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import com.partner.integration.git.GitHandler;

public class AutomatedResolutionWorkItemHandler implements WorkItemHandler {
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		HashMap<String,Object> results = new HashMap<String,Object>();
		GitHandler gitHanlder = new GitHandler();
		String result = gitHanlder.overwriteWithRemoteRepo();
		results.put("errorMessage", result);
		manager.completeWorkItem(workItem.getId(), results);	
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// TODO Auto-generated method stub
	}

	

}
