package com.atolcd.alfresco;

import java.util.ArrayList;
import java.util.List;

public class AtolAuthorityParameters {
	private long qnameId = 0;
	private List<String> groupNames = null;
	
	public AtolAuthorityParameters(){	
		groupNames = new ArrayList<String>();
	}
	
	public long getQnameId() {
		return qnameId;
	}

	public void setQnameId(long qnameId) {
		this.qnameId = qnameId;
	}

	public List<String> getGroupNames() {
		return groupNames;
	}

	public void setGroupNames(List<String> groupNames) {
		this.groupNames = groupNames;
	}
	
	public void setSite(String siteName){
		groupNames.add("GROUP_site_"+siteName+"_SiteManager");
		groupNames.add("GROUP_site_"+siteName+"_SiteCollaborator");
		groupNames.add("GROUP_site_"+siteName+"_SiteContributor");
		groupNames.add("GROUP_site_"+siteName+"_SiteConsumer");
	}
}
