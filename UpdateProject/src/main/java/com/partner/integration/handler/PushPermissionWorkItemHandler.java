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
		pushPermissionhandler.checkPushPermission();
		manager.completeWorkItem(workItem.getId(), new HashMap<String,Object>());
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// TODO Auto-generated method stub
	}

}
