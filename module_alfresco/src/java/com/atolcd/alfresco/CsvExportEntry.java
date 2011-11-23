package com.atolcd.alfresco;

public class CsvExportEntry {
	long id = 0;
	String auditSite = "";
	String auditAppName = "";
	String auditActionName = "";
	int count = 0;;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAuditSite() {
		return auditSite;
	}

	public void setAuditSite(String auditSite) {
		this.auditSite = auditSite;
	}

	public String getAuditAppName() {
		return auditAppName;
	}

	public void setAuditAppName(String auditAppName) {
		this.auditAppName = auditAppName;
	}

	public String getAuditActionName() {
		return auditActionName;
	}

	public void setAuditActionName(String auditActionName) {
		this.auditActionName = auditActionName;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
