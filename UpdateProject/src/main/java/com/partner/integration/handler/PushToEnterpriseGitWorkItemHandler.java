package com.partner.integration.handler;

import java.util.HashMap;

import org.eclipse.jgit.util.StringUtils;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import com.partner.integration.git.GitHandler;

public class PushToEnterpriseGitWorkItemHandler implements WorkItemHandler {

	GitHandler gitHandler = new  GitHandler();
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String failureMessage = gitHandler.updateEnterpriseGitRepository();
		Boolean success = StringUtils.isEmptyOrNull(failureMessage);
		
		HashMap<String, Object> results = new HashMap<String,Object>();
		results.put("pushSuccess", success);
		results.put("failureMessage", failureMessage);
		manager.completeWorkItem(workItem.getId(), results);
	}

	@Override
	public void abortWorkItem(WorkItem arg0, WorkItemManager arg1) {
		// TODO Auto-generated method stub
		
	}

}
