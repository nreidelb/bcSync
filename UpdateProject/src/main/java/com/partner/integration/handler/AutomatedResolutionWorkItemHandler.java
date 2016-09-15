package com.partner.integration.handler;

import java.util.HashMap;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import com.partner.integration.conflictresolution.AutomatedResolutionHandler;

public class AutomatedResolutionWorkItemHandler implements WorkItemHandler {
	
	private AutomatedResolutionHandler automatedResolutionHandler = new AutomatedResolutionHandler();
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		automatedResolutionHandler.automatedResolution();
		manager.completeWorkItem(workItem.getId(), new HashMap<String,Object>());	
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// TODO Auto-generated method stub
	}

	

}
