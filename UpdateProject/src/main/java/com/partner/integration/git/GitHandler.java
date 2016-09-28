package com.partner.integration.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.RemoteRefUpdate.Status;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jboss.logging.Logger;

import com.partner.integration.git.GitHandler.PullException;
import com.partner.utility.PropertiesLoader;

public class GitHandler {

	private static final Logger log = Logger.getLogger(GitHandler.class.getName());
	
	private static final String MASTER_BRANCH_NAME = "master";
	private static final String REMOTE_REPO_NAME = "remoteRepo";
	private static final String BUSINESS_CENTRAL_REPO_NAME = "businessCentral";
	
	private final Properties GIT_HUB_PROPERTIES;
	private final Properties BC_PROPERTIES;
	private final Properties GENERAL_PROPERTIES;	
	private final String GIT_REPO_LOCATION;
	private final UsernamePasswordCredentialsProvider AUTOMATED_USER_CREDENTIALS_PROVIDER;
	private final UsernamePasswordCredentialsProvider BPMS_ADMIN_CREDENTIALS_PROVIDER;

	private final String BPMS_REPO_URL;
	private final String REMOTE_REPO_URL;
	private final Repo REMOTE;
	private final Repo BUSINESS_CENTRAL;
	
	public GitHandler(){
		GIT_HUB_PROPERTIES = PropertiesLoader.loadPropertiesFile("github.properties");
		BC_PROPERTIES = PropertiesLoader.loadPropertiesFile("business-central.properties");
		GENERAL_PROPERTIES = PropertiesLoader.loadPropertiesFile("general.properties");
		
		GIT_REPO_LOCATION = GENERAL_PROPERTIES.getProperty("localRepoLocation");
		BPMS_REPO_URL = BC_PROPERTIES.getProperty("repoUrl");
		REMOTE_REPO_URL = GIT_HUB_PROPERTIES.getProperty("repoUrl");
		
		AUTOMATED_USER_CREDENTIALS_PROVIDER = new UsernamePasswordCredentialsProvider(GIT_HUB_PROPERTIES.getProperty("userName"), GIT_HUB_PROPERTIES.getProperty("password"));
		BPMS_ADMIN_CREDENTIALS_PROVIDER = new UsernamePasswordCredentialsProvider(BC_PROPERTIES.getProperty("userName"), BC_PROPERTIES.getProperty("password"));
		
		REMOTE = new Repo(REMOTE_REPO_NAME, REMOTE_REPO_URL, AUTOMATED_USER_CREDENTIALS_PROVIDER);
		BUSINESS_CENTRAL = new Repo(BUSINESS_CENTRAL_REPO_NAME,BPMS_REPO_URL, BPMS_ADMIN_CREDENTIALS_PROVIDER);
	}

	//Returns failure message or null if process was a success
	public String updateBusinessCentralRepo(){
		return pullThenPush(REMOTE, BUSINESS_CENTRAL, MASTER_BRANCH_NAME);
	}

	//Returns failure message or null if process was a success
	public String updateEnterpriseGitRepository(){
		return pullThenPush(BUSINESS_CENTRAL, REMOTE, MASTER_BRANCH_NAME);
	}
	
	//Returns failure message or null if process was a success
	public String pullRequestToEnterpriseGit(String branch){
		return pullThenPush(BUSINESS_CENTRAL, REMOTE, branch);
	}

	private String pullThenPush(Repo pull, Repo push, String toBranch) {
		String failureMessage = null;
		Git git = null;

		try {
			git = createReferenceToLocalRepository();
			
			configureRemoteRepositories(git);
			
			try{
				git.checkout().setName(MASTER_BRANCH_NAME).call();
				git.pull().setRemote(pull.getName()).setRemoteBranchName(MASTER_BRANCH_NAME).setCredentialsProvider(pull.getCredentials()).call();
				if(StringUtils.isEmpty(toBranch)){
					toBranch = findDefaultBranch(git);
					log.warn("no branch specified for pull request, defaulting to " + toBranch);
				}
				try{
					git.checkout().setName(toBranch).setCreateBranch(true).call();
				} catch(RefAlreadyExistsException e){
					//If branch already exists, just check it out, don't create it
					Ref result = git.checkout().setName(toBranch).call();
					//Make sure the branch has the latest changes from master
					git.rebase().setUpstream(MASTER_BRANCH_NAME).call();
					log.debug("Current Branch:" + result.getName());
				}
				RefSpec branchToBranch = new RefSpec(toBranch+":"+toBranch);
				
				Iterable<PushResult> results = git.push().setRemote(push.getName()).setCredentialsProvider(push.getCredentials()).setRefSpecs(branchToBranch).call();
				for(PushResult result:results){
					for(RemoteRefUpdate update:result.getRemoteUpdates()){
						if(!Status.OK.equals(update.getStatus()) && !Status.UP_TO_DATE.equals(update.getStatus())){
							failureMessage = "Push to "+ push.getName() + " failed with status: " + update.getStatus() 
								+ " and message: " + update.getMessage();
							break;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				failureMessage = e.getMessage();
			}			
		} catch (IOException e) {
			failureMessage = e.getMessage();
		} catch (NoWorkTreeException e) {
			failureMessage = e.getMessage();
		} catch (GitAPIException e) {
			failureMessage = e.getMessage();
		} finally {
			if(git != null){
				git.close();
			}
		}
		if(!StringUtils.isEmpty(failureMessage)){
			log.error(failureMessage);
			return  failureMessage;
		}
		return null;
	}
	
	public String findDefaultBranch(){
		return findDefaultBranch(null);
	}
	
	//Gets a semi unique identifier for a branch to use
	private String findDefaultBranch(Git git) {
		boolean needToClose = false;
		try{
		if(git == null){
			needToClose = true;
			git = createReferenceToLocalRepository();
		}
		Iterable<RevCommit> revisionLog = git.log().call();
	    for (Iterator<RevCommit> iterator = revisionLog.iterator(); iterator.hasNext();) {
		    RevCommit rev = iterator.next();
		    PersonIdent author = rev.getAuthorIdent();
			if(author!= null && author.getName() != null){
		    	  return StringUtils.deleteWhitespace(author.getName());
		      }
	    }
	    for (Iterator<RevCommit> iterator = revisionLog.iterator(); iterator.hasNext();) {
		    RevCommit rev = iterator.next();
		    PersonIdent author = rev.getAuthorIdent();
			if(author!= null && author.getEmailAddress() != null){
		    	  return StringUtils.deleteWhitespace(author.getEmailAddress());
		      }
		}
		}catch(Exception e){
			log.error(e.getMessage());
		} finally {
			if(needToClose){
				git.close();
			}
		}
	    log.error("Could not find a commit with a useable branch name.");
	    return null;
	}
	
	//Be careful to use this only after the latest commits are in the local repo.
	public String findDefaultTitle() {
		Git git = null;
		try {
			git = createReferenceToLocalRepository();
		
		Iterable<RevCommit> revisionLog = git.log().call();
	    for (Iterator<RevCommit> iterator = revisionLog.iterator(); iterator.hasNext();) {
	      RevCommit rev = iterator.next();
	      if(!StringUtils.isEmpty(rev.getShortMessage())){
	    	  return rev.getAuthorIdent().getName();
	      }
	    }
		} catch (IOException e) {
			log.error(e.getMessage());
		} catch (NoHeadException e) {
			log.error(e.getMessage());
		} catch (GitAPIException e) {
			log.error(e.getMessage());
		}finally {
			if(git != null){
				git.close();
			}
		}
		log.error("Could not find a commit with a useable pull request title.");
	    return null;
	}

	//This method overwrites the master branch of Business Central with the contents of the remote repo
	public String overwriteWithRemoteRepo(){
		String failureMessage = null;
		Git git = null;
		try {
			git = createReferenceToLocalRepository();			
			configureRemoteRepositories(git);
			git.fetch().setRemote(REMOTE_REPO_NAME).call();
			git.reset().setMode(ResetType.HARD).setRef(REMOTE_REPO_NAME + "/" + MASTER_BRANCH_NAME).call();
			git.push().setRemote(BUSINESS_CENTRAL_REPO_NAME).setForce(true).call();
		} catch (IOException | GitAPIException e) {
			failureMessage = e.getMessage();
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if(git != null){
				git.close();
			}
		}
		if(!StringUtils.isEmpty(failureMessage)){
			log.error(failureMessage);
			return  failureMessage;
		}
		return null;	
	}
	
	//Only when pulling to
	public Set<String> findUsersResponsibleForChanges() throws PullException{
		String failureMessage = null;
		Git git = null;
		Set<String> usersWhoComitted = new HashSet<String>();

		try {
			git = createReferenceToLocalRepository();
			
			configureRemoteRepositories(git);
			//Ensure changes from BC are reflected locally
			git.checkout().setName(MASTER_BRANCH_NAME).call();
			git.pull().setRemote(BUSINESS_CENTRAL.getName()).setRemoteBranchName(MASTER_BRANCH_NAME).setCredentialsProvider(BUSINESS_CENTRAL.getCredentials()).call();
			
			Iterator<RevCommit> gitLogsBC = git.log().call().iterator();
			git.fetch().setRemote(REMOTE.getName()).call();
			
			Iterator<RevCommit> itLogsRemote = git.log().addPath(REMOTE.getName()+ "/" + MASTER_BRANCH_NAME).call().iterator();
			RevCommit firstRemoteCommit = itLogsRemote.next();
			while(gitLogsBC.hasNext()){
				RevCommit next = gitLogsBC.next();
				if(next.getId().equals(firstRemoteCommit.getId())){
					break;
				} else {
					usersWhoComitted.add(next.getAuthorIdent().getName());
				}
			}
		} catch(Exception e){
			log.error(e.getMessage());
			git.close();
			throw new PullException();
		}
		return usersWhoComitted;
	}

	private void configureRemoteRepositories(Git git) throws IOException {
		StoredConfig config = git.getRepository().getConfig();
		config.setString("remote",BUSINESS_CENTRAL_REPO_NAME,"url",BPMS_REPO_URL);
		config.setString("remote", BUSINESS_CENTRAL_REPO_NAME, "fetch", "+refs/heads/*:refs/remotes/businessCental/*");
		config.setString("remote",REMOTE_REPO_NAME,"url",REMOTE_REPO_URL);
		config.setString("remote", REMOTE_REPO_NAME, "fetch", "+refs/heads/*:refs/remotes/remoteRepo/*");
		config.save();
	}
	


	private Git createReferenceToLocalRepository() throws IOException, NoWorkTreeException, GitAPIException {
		Git git;
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		   
		Repository repository = builder.setGitDir(new File(GIT_REPO_LOCATION))
		        .readEnvironment()
		        .findGitDir()
		        .build();
		
		git = new Git(repository);
		
		//If there are conflicts, clear out the local repo since conflicts will be resolved by a developer elsewhere
		if(!git.status().call().getConflicting().isEmpty()){
			git.reset().setRef("HEAD~").setMode(ResetType.HARD).call();
		}
		return git;
	}
	
	public static class Repo{
		private String name;
		private String url;
		private UsernamePasswordCredentialsProvider credentials;
		
		public Repo (String name, String url, UsernamePasswordCredentialsProvider credentialsProvider){
			this.name = name;
			this.url = url;
			this.credentials = credentialsProvider;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getUrl() {
			return url;
		}
		
		public void setUrl(String url) {
			this.url = url;
		}

		public UsernamePasswordCredentialsProvider getCredentials() {
			return credentials;
		}

		public void setCredentials(UsernamePasswordCredentialsProvider credentials) {
			this.credentials = credentials;
		}
	}

	public class PullException extends Exception {

	}

}
