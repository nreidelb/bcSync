package com.partner.integration.handler;

import java.util.HashMap;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import com.partner.integration.git.PushPermissionHandler;

public class PushPermissionWorkItemHandler implements WorkItemHandler {
	
	private PushPermissionHandler pushPermissionhandler = new PushPermissionHandler();

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		HashMap<String,Object> results = new HashMap<String,Object>();
		results.put("baHasPushPermission", pushPermissionhandler.checkPushPermission());
		manager.completeWorkItem(workItem.getId(), results);
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// TODO Auto-generated method stub
	}

}
