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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class AuditQueryParameters {
  private String       siteId;
  private List<String> sitesId;
  private String       appName;
  private List<String> appNames;
  private String       actionName;
  private List<String> actionNames;
  private String       object;
  private long         dateFrom;
  private long         dateTo;
  private String       slicedDates;
  private String       userId;
  private List<String> userIds;
  private int          limit;
  private String       nodeType;
  private List<String> nodeTypes;
  private List<String> groupsMembers;

  public AuditQueryParameters() {
    siteId = null;
    sitesId = null;
    appName = null;
    appNames = null;
    actionName = null;
    actionNames = null;
    object = null;
    dateFrom = 0;
    dateTo = 0;
    slicedDates = null;
    userId = null;
    userIds = null;
    nodeType = null;
    nodeTypes = null;
    groupsMembers = null;
  }

  public String getSlicedDates() {
    return slicedDates;
  }

  public void setSlicedDates(String slicedDates) {
    this.slicedDates = slicedDates;
  }

  public List<String> getSitesId() {
    return sitesId;
  }

  public void setSitesId(List<String> sitesId) {
    if (sitesId == null || sitesId.isEmpty()) {
      this.sitesId = null;
    } else {
      this.sitesId = sitesId;
    }
  }

  public void setSitesId(String sitesId) {
    if (sitesId != null) {
      String[] sitesToken = sitesId.split(",");
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

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public List<String> getAppNames() {
    return appNames;
  }

  public void setAppNames(String appNames) {
    if (appNames != null) {
      String[] appNamesToken = appNames.split(",");
      this.appNames = new ArrayList<>(appNamesToken.length);
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

  public List<String> getActionNames() {
    return actionNames;
  }

  public void setActionNames(String actionNames) {
    if (actionNames != null) {
      String[] actionNamesToken = actionNames.split(",");
      this.actionNames = new ArrayList<>(actionNamesToken.length);
      for (String token : actionNamesToken) {
        this.actionNames.add(token);
      }
    }
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public List<String> getUserIds() {
    return userIds;
  }

  public void setUserIds(String userIds) {
    if (userIds != null) {
      String[] userIdsToken = userIds.split(",");
      this.userIds = new ArrayList<>(userIdsToken.length);
      for (String token : userIdsToken) {
        this.userIds.add(token);
      }
    }
  }

  public long getDateFrom() {
    return dateFrom;
  }

  public void setDateFrom(long dateFrom) {
    this.dateFrom = dateFrom;
  }

  public void setDateFrom(String dateFrom) {
    if (dateFrom == null || "".equals(dateFrom)) {
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
    if (dateTo == null || "".equals(dateTo)) {
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

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public String getNodeType() {
    return nodeType;
  }

  public void setNodeType(String nodeType) {
    this.nodeType = nodeType;
  }

  public List<String> getNodeTypes() {
    return nodeTypes;
  }

  public void setNodeTypes(List<String> nodeTypes) {
    if (nodeTypes == null || nodeTypes.isEmpty()) {
      this.nodeTypes = null;
    } else {
      this.nodeTypes = nodeTypes;
    }
  }

  public void setNodeTypes(String nodeTypes) {
    if (nodeTypes != null) {
      String[] nodeTypesToken = nodeTypes.split(",");
      this.nodeTypes = new ArrayList<>(nodeTypesToken.length);
      for (String token : nodeTypesToken) {
        this.nodeTypes.add(token);
      }
    }
  }

  public List<String> getGroupsMembers() {
    return groupsMembers;
  }

  public void setGroupsMembers(List<String> groupsMembers) {
    if (groupsMembers == null || groupsMembers.isEmpty()) {
      this.groupsMembers = Collections.emptyList();
    } else {
      this.groupsMembers = groupsMembers;
    }
  }

  public void setGroupsMembers(String groupsMembers) {
    if (groupsMembers != null) {
      String[] groupsMembersToken = groupsMembers.split(",");
      this.groupsMembers = new ArrayList<>(groupsMembersToken.length);
      for (String token : groupsMembersToken) {
        this.groupsMembers.add(token);
      }
    }
  }

  public String toJSON() throws JSONException {
    JSONObject jsonResult = new JSONObject();
    jsonResult.put("siteId", siteId);
    jsonResult.put("sitesId", sitesId);
    jsonResult.put("appName", appName);
    jsonResult.put("appNames", appNames);
    jsonResult.put("actionName", actionName);
    jsonResult.put("actionNames", actionNames);
    jsonResult.put("object", object);
    jsonResult.put("userId", userId);
    jsonResult.put("userIds", userIds);
    jsonResult.put("dateFrom", Long.toString(dateFrom));
    jsonResult.put("dateTo", Long.toString(dateTo));
    jsonResult.put("slicedDates", slicedDates);
    jsonResult.put("limit", Integer.toString(limit));
    jsonResult.put("nodeType", nodeType);
    jsonResult.put("nodeTypes", nodeTypes);
    jsonResult.put("groupsMembers", groupsMembers);

    return jsonResult.toString();
  }
}
