package com.atolcd.alfresco;

import java.util.ArrayList;
import java.util.List;

public class AtolAuthorityParameters {
	private long memberQnameId = 0;
	private long personQnameId = 0;
	private List<String> groupNames = null;

	public AtolAuthorityParameters() {
		groupNames = new ArrayList<String>();
	}

	public long getMemberQnameId() {
		return memberQnameId;
	}

	public void setMemberQnameId(long memberQnameId) {
		this.memberQnameId = memberQnameId;
	}

	public long getPersonQnameId() {
		return personQnameId;
	}

	public void setPersonQnameId(long personQnameId) {
		this.personQnameId = personQnameId;
	}

	public List<String> getGroupNames() {
		return groupNames;
	}

	public void setGroupNames(List<String> groupNames) {
		this.groupNames = groupNames;
	}

	public void setSite(String siteName) {
		groupNames.add("GROUP_site_" + siteName + "_SiteManager");
		groupNames.add("GROUP_site_" + siteName + "_SiteCollaborator");
		groupNames.add("GROUP_site_" + siteName + "_SiteContributor");
		groupNames.add("GROUP_site_" + siteName + "_SiteConsumer");
	}
}
