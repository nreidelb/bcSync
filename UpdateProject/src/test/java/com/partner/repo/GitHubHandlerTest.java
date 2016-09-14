package com.partner.repo;

import org.junit.Ignore;
import org.junit.Test;

import com.partner.integration.repo.GitHubHandler;

public class GitHubHandlerTest {
	
	//Ignore tests used locally which require setup
	@Test
	@Ignore
	public void createPullRequestTest(){
		GitHubHandler gitHub = new GitHubHandler();
		gitHub.createPullRequest("pullRequest", "someMessage", "testBranch");
	}

}
