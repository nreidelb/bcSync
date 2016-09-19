package com.partner.integration.handler;

import java.util.HashMap;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import com.partner.integration.conflictresolution.ResolutionStrategyHandler;

public class ResolutionStrategyWorkItemHandler implements WorkItemHandler {
	
	private ResolutionStrategyHandler resolutionStrategyHandler = new ResolutionStrategyHandler();

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		HashMap<String,Object> results = new HashMap<String,Object>();
		results.put("manualResolutionRequired", resolutionStrategyHandler.determineResolutionStrategy());
		manager.completeWorkItem(workItem.getId(), results);
	}
	
	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// TODO Auto-generated method stub
	}

}
