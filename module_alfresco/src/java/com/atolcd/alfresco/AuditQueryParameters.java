package com.atolcd.alfresco;

import java.util.ArrayList;
import java.util.List;

public class AuditQueryParameters {

	private String siteId;
	private List<String> sitesId;
	private String appName;
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
		actionName = null;
		object = null;
		dateFrom = 0;
		dateTo = 0;
		slicedDates = null;
		userId = null;
	}

	public AuditQueryParameters(String site, List<String> sites, String app, String action, String obj, long from, long to, String dates, String user) {
		siteId = site;
		sitesId = sites;
		appName = app;
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

	public void setSitesId(List<String> sitesId) {
		this.sitesId = sitesId;
	}

	// Surcharge du set pour la construction à partir d'une String
	public void setSitesId(String sitesId) {
		if (sitesId != null) {
			this.sitesId = new ArrayList<String>();
			String[] sitesToken = sitesId.split(",");
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

	public void setAppName(String appName) {
		this.appName = appName;
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