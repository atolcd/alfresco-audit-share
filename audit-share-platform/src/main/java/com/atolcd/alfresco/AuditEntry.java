/*--
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

import org.json.JSONException;
import org.json.JSONObject;

public class AuditEntry {
  long   id              = 0;
  String auditUserId     = "";
  String auditSite       = "";
  String auditAppName    = "";
  String auditActionName = "";
  String auditObject     = "";
  long   auditTime       = 0;
  String auditNodeType   = "";

  public AuditEntry() {
  }

  public AuditEntry(String json) throws JSONException {
    if (json != null && json.length() > 0) {
      JSONObject jsonObj = new JSONObject(json);
      this.id = jsonObj.getLong("id");
      this.auditUserId = jsonObj.getString("auditUserId");
      this.auditSite = jsonObj.getString("auditSite");
      this.auditAppName = jsonObj.getString("auditAppName");
      this.auditActionName = jsonObj.getString("auditActionName");
      this.auditObject = jsonObj.getString("auditObject");
      this.auditTime = jsonObj.getLong("auditTime");

      if (jsonObj.has("auditNodeType")) {
        this.auditNodeType = jsonObj.getString("auditNodeType");
      }
    }
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getId() {
    return this.id;
  }

  public String getAuditUserId() {
    return auditUserId;
  }

  public void setAuditUserId(String auditUserId) {
    this.auditUserId = auditUserId;
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

  public String getAuditObject() {
    return auditObject;
  }

  public void setAuditObject(String auditObject) {
    this.auditObject = auditObject;
  }

  public long getAuditTime() {
    return auditTime;
  }

  public void setAuditTime(long auditTime) {
    this.auditTime = auditTime;
  }

  public String getAuditNodeType() {
    return auditNodeType;
  }

  public void setAuditNodeType(String auditNodeType) {
    this.auditNodeType = auditNodeType;
  }

  public String toJSON() throws JSONException {
    JSONObject jsonResult = new JSONObject();
    jsonResult.put("id", Long.toString(id));
    jsonResult.put("auditUserId", auditUserId);
    jsonResult.put("auditSite", auditSite);
    jsonResult.put("auditAppName", auditAppName);
    jsonResult.put("auditActionName", auditActionName);
    jsonResult.put("auditObject", auditObject);
    jsonResult.put("auditTime", Long.toString(auditTime));
    jsonResult.put("auditNodeType", auditNodeType);

    return jsonResult.toString();
  }
}
