package com.partner.integration.handler;

import java.util.HashMap;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import com.partner.integration.repo.GitHubHandler;

public class CreatePullRequestWorkItemHandler implements WorkItemHandler {

	GitHubHandler gitHubHandler = new GitHubHandler();
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String title = (String) workItem.getParameter("title");
		String message = (String) workItem.getParameter("message");
		String branch = (String) workItem.getParameter("branch");
		
		HashMap<String,Object> results = new HashMap<String,Object>();
		
		gitHubHandler.createPullRequest(title, message, branch);
		manager.completeWorkItem(workItem.getId(), results);
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// TODO Auto-generated method stub

	}

}
