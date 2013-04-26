/*
 * Copyright (C) 2013 Atol Conseils et Développements.
 * http://www.atolcd.com/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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