/*
 * Copyright (C) 2018 Atol Conseils et Développements.
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

public class AuditObjectPopularity {
	private int popularity;
	private String auditObject; // 'audit_object' database field
	private String objectName;
	private String objectDisplayName;
	private String auditSite;
	private String siteComponent;

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

	public String getSiteComponent() {
		return siteComponent;
	}

	public void setSiteComponent(String siteComponent) {
		this.siteComponent = siteComponent;
	}
}