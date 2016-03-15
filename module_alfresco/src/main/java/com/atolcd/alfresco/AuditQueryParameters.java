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

public class AuditQueryParameters {
	private String siteId;
	private List<String> sitesId;
	private String appName;
	private List<String> appNames;
	private String actionName;
	private String object;
	private long dateFrom;
	private long dateTo;
	private String slicedDates;
	private String userId;

	public String getSlicedDates() {
		return slicedDates;
	}

	public void setSlicedDates(String slicedDates) {
		this.slicedDates = slicedDates;
	}

	public AuditQueryParameters() {
		siteId = null;
		sitesId = null;
		appName = null;
		appNames = null;
		actionName = null;
		object = null;
		dateFrom = 0;
		dateTo = 0;
		slicedDates = null;
		userId = null;
	}

	public AuditQueryParameters(String site, List<String> sites, String app, List<String> apps, String action, String obj, long from,
			long to, String dates, String user) {
		siteId = site;
		sitesId = sites;
		appName = app;
		appNames = apps;
		actionName = action;
		object = obj;
		dateFrom = from;
		dateTo = to;
		slicedDates = dates;
		userId = user;
	}

	public List<String> getSitesId() {
		return sitesId;
	}

	public void setSitesId(List<String> _sitesId) {
		if (_sitesId == null || (_sitesId != null && _sitesId.isEmpty())) {
			this.sitesId = null;
		} else {
			this.sitesId = _sitesId;
		}
	}

	public void setSitesId(String _sitesId) {
		if (_sitesId != null) {
			String[] sitesToken = _sitesId.split(",");
			this.sitesId = new ArrayList<String>(sitesToken.length);
			for (String token : sitesToken) {
				this.sitesId.add(token);
			}
		}
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String _appName) {
		this.appName = _appName;
	}

	public List<String> getAppNames() {
		return appNames;
	}

	public void setAppNames(String _appNames) {
		if (_appNames != null) {
			String[] appNamesToken = _appNames.split(",");
			this.appNames = new ArrayList<String>(appNamesToken.length);
			for (String token : appNamesToken) {
				this.appNames.add(token);
			}
		}
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public long getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(long dateFrom) {
		this.dateFrom = dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		if (dateFrom == null || dateFrom.equals("")) {
			this.dateFrom = 0;
		} else {
			this.dateFrom = Long.parseLong(dateFrom);
		}
	}

	public long getDateTo() {
		return dateTo;
	}

	public void setDateTo(long dateTo) {
		this.dateTo = dateTo;
	}

	public void setDateTo(String dateTo) {
		if (dateTo == null || dateTo.equals("")) {
			this.dateTo = 0;
		} else {
			this.dateTo = Long.parseLong(dateTo);
		}
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}
}