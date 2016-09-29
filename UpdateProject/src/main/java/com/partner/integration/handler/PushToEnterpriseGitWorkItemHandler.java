package com.partner.integration.handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.util.StringUtils;
import org.kie.api.builder.Results;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import com.partner.integration.git.GitHandler;
import com.partner.integration.git.GitHandler.PullException;

public class PushToEnterpriseGitWorkItemHandler implements WorkItemHandler {

	GitHandler gitHandler = new  GitHandler();
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		Map<String, Object> results = new HashMap<String,Object>();
		Set<String> users = new HashSet<String>();
		try {
			users = gitHandler.findUsersResponsibleForChanges();
			
		} catch (PullException e) {
			//Do nothing, the error will have to be handled elsewhere;
		}
		String failureMessage = gitHandler.updateEnterpriseGitRepository();
		
		Boolean success = StringUtils.isEmptyOrNull(failureMessage);
		
		results.put("pushSuccess", success);
		results.put("failureMessage", failureMessage);
		results.put("users", users);
		manager.completeWorkItem(workItem.getId(), results);
	}

	@Override
	public void abortWorkItem(WorkItem arg0, WorkItemManager arg1) {
		// TODO Auto-generated method stub
		
	}

}
