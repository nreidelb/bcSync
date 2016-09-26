package com.partner.integration.handler;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import com.partner.integration.git.GitHandler;
import com.partner.integration.repo.GitHubHandler;

public class CreatePullRequestWorkItemHandler implements WorkItemHandler {
	
	private static final Logger log = Logger.getLogger(CreatePullRequestWorkItemHandler.class.getName());

	GitHubHandler gitHubHandler = new GitHubHandler();
	
	GitHandler gitHandler = new GitHandler();
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String title = (String) workItem.getParameter("title");
		String message = (String) workItem.getParameter("message");
		String branch = (String) workItem.getParameter("branch");
		
		HashMap<String,Object> results = new HashMap<String,Object>();
		
		gitHandler.pullRequestToEnterpriseGit(branch);
		
		if(StringUtils.isEmpty(title)){
			title = gitHandler.findDefaultTitle();
			log.warn("No pull request title specified name specified, defaulting title to " + title );
		}
		
		gitHubHandler.createPullRequest(title, message, branch);
		manager.completeWorkItem(workItem.getId(), results);
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// TODO Auto-generated method stub

	}

}
