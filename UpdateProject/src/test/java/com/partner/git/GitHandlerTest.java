package com.partner.git;

import org.junit.Ignore;
import org.junit.Test;

import com.partner.integration.git.GitHandler;

public class GitHandlerTest {
	
	
	//Ignore tests used locally which require setup
		@Test
		@Ignore
	public void updateBusinessCentralRepoTest(){
		GitHandler gitHandler = new GitHandler();
		gitHandler.updateBusinessCentralRepo();
	}
	
		//Ignore tests used locally which require setup
		@Test
		@Ignore
	public void updateGitRepo(){
		GitHandler gitHandler = new GitHandler();
		gitHandler.updateEnterpriseGitRepository();;
	}
	
		//Ignore tests used locally which require setup
		@Test
		@Ignore
	public void pullRequestToEnterpriseGitTest(){
		GitHandler gitHandler = new GitHandler();
		gitHandler.pullRequestToEnterpriseGit("testBranch");
		
	}

}
