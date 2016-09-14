package com.partner.integration.git;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.RemoteRefUpdate.Status;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jboss.logging.Logger;

import com.partner.utility.PropertiesLoader;

public class GitHandler {
	
	private static final Logger log = Logger.getLogger(GitHandler.class.getName());
	
	private static final Properties GIT_HUB_PROPERTIES = PropertiesLoader.loadPropertiesFile("github.properties");
	private static final Properties BC_PROPERTIES = PropertiesLoader.loadPropertiesFile("business-central.properties");
	private static final Properties GENERAL_PROPERTIES = PropertiesLoader.loadPropertiesFile("general.properties");
	
	private static final String MASTER_BRANCH_NAME = "master";
	private static final String GIT_REPO_LOCATION = GENERAL_PROPERTIES.getProperty("localRepoLocation");
	private static final UsernamePasswordCredentialsProvider REIDELBOT_CREDENTIALS_PROVIDER = new UsernamePasswordCredentialsProvider(GIT_HUB_PROPERTIES.getProperty("userName"), GIT_HUB_PROPERTIES.getProperty("password"));
	private static final UsernamePasswordCredentialsProvider BPMS_ADMIN_CREDENTIALS_PROVIDER = new UsernamePasswordCredentialsProvider(BC_PROPERTIES.getProperty("userName"), BC_PROPERTIES.getProperty("password"));
	private static final String REMOTE_REPO_NAME = "remoteRepo";
	private static final String BUSINESS_CENTRAL_REPO_NAME = "businessCentral";
	private static final String BPMS_REPO_URL = BC_PROPERTIES.getProperty("repoUrl");
	private static final String REMOTE_REPO_URL = GIT_HUB_PROPERTIES.getProperty("repoUrl");
	private static final Repo REMOTE = new Repo(REMOTE_REPO_NAME, REMOTE_REPO_URL, REIDELBOT_CREDENTIALS_PROVIDER);
	private static final Repo BUSINESS_CENTRAL = new Repo(BUSINESS_CENTRAL_REPO_NAME,BPMS_REPO_URL, BPMS_ADMIN_CREDENTIALS_PROVIDER);

	public void updateBusinessCentralRepo(){
		pullThenPush(REMOTE, BUSINESS_CENTRAL, MASTER_BRANCH_NAME);
	}

	
	public void updateEnterpriseGitRepository(){
		pullThenPush(BUSINESS_CENTRAL, REMOTE, MASTER_BRANCH_NAME);
	}
	
	public void pullRequestToEnterpriseGit(String branch){
		pullThenPush(BUSINESS_CENTRAL, REMOTE, branch);
	}

	private void pullThenPush(Repo pull, Repo push, String toBranch) {
		boolean sucess = true;
		Exception failureException = null;
		Git git = null;

		try {
			git = createReferenceToLocalRepository();
			
			configureRemoteRepositories(git);
			
			try{
				git.checkout().setName(MASTER_BRANCH_NAME).call();
				git.pull().setRemote(pull.getName()).setRemoteBranchName(MASTER_BRANCH_NAME).setCredentialsProvider(pull.getCredentials()).call();
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
							log.info("Push to "+ push.getName() + " failed with status: " + update.getStatus() 
								+ " and message: " + update.getMessage());
							sucess = false;
							break;
						}
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage());
				failureException = e;
				sucess = false;
			}			
		} catch (IOException e) {
			failureException = e;
			log.error(e.getMessage());
			sucess = false;
		} finally {
			if(git != null){
				git.close();
			}
			if(!sucess){
				//TODO:Email about failure
			}
		}
	}

	private void configureRemoteRepositories(Git git) throws IOException {
		StoredConfig config = git.getRepository().getConfig();
		config.setString("remote",BUSINESS_CENTRAL_REPO_NAME,"url",BPMS_REPO_URL);
		config.setString("remote", BUSINESS_CENTRAL_REPO_NAME, "fetch", "+refs/heads/*:refs/remotes/businessCental/*");
		config.setString("remote",REMOTE_REPO_NAME,"url",REMOTE_REPO_URL);
		config.setString("remote", REMOTE_REPO_NAME, "fetch", "+refs/heads/*:refs/remotes/remoteRepo/*");
		config.save();
	}

	private Git createReferenceToLocalRepository() throws IOException {
		Git git;
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		   
		Repository repository = builder.setGitDir(new File(GIT_REPO_LOCATION))
		        .readEnvironment()
		        .findGitDir()
		        .build();
		
		git = new Git(repository);
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

}
