package com.atolcd.alfresco;

public class AuditObjectPopularity {
	private int popularity;
	private String auditObject; // Valeur du champ audit_object en base
	private String objectName;
	private String objectDisplayName;
	private String auditSite;

	public AuditObjectPopularity() {

	}

	public int getPopularity() {
		return popularity;
	}

	public void setPopularity(int popularity) {
		this.popularity = popularity;
	}

	public String getAuditObject() {
		return auditObject;
	}

	public void setAuditObject(String auditObject) {
		this.auditObject = auditObject;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public String getObjectDisplayName() {
		return objectDisplayName;
	}

	public void setObjectDisplayName(String objectDisplayName) {
		this.objectDisplayName = objectDisplayName;
	}

	public String getAuditSite() {
		return auditSite;
	}

	public void setAuditSite(String auditSite) {
		this.auditSite = auditSite;
	}

}
