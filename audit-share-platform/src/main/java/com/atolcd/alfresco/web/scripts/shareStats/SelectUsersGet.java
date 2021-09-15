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
package com.atolcd.alfresco.web.scripts.shareStats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.Assert;

import com.atolcd.alfresco.AtolAuthorityParameters;
import com.atolcd.alfresco.AuditQueryParameters;
import com.atolcd.alfresco.helper.PermissionsHelper;

public class SelectUsersGet extends DeclarativeWebScript implements InitializingBean {
  // Logger
  private static final Log logger = LogFactory.getLog(SelectUsersGet.class);

  private SqlSessionTemplate sqlSessionTemplate;
  private NodeService nodeService;
  private SiteService siteService;
  private AuthorityService authorityService;
  private Long memberQnameId = null;
  private Long personQnameId = null;
  private Long containerQnameId = null;

  private static final String SELECT_CONNECTED_USERS = "alfresco.atolcd.audit.selectConnectedUsers";
  private static final String SELECT_QNAME_ID = "alfresco.atolcd.audit.selectQNameId";
  private static final String SELECT_SITES_MEMBERS = "alfresco.atolcd.audit.selectSiteMember";

  private static final String PARAM_GROUPS = "groups";

  public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setSiteService(SiteService siteService) {
    this.siteService = siteService;
  }

  public void setAuthorityService(AuthorityService authorityService) {
    this.authorityService = authorityService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(this.sqlSessionTemplate);
    Assert.notNull(this.nodeService);
    Assert.notNull(this.siteService);
    Assert.notNull(this.authorityService);
  }

  @Override
  protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
    try {
      Map<String, Object> model = new HashMap<String, Object>();
      if (PermissionsHelper.isAuthorized(req)) {
        if (this.sqlSessionTemplate != null) {
          String type = req.getParameter("type");
          AuditQueryParameters auditQueryParameters = buildParametersFromRequest(req);
          AtolAuthorityParameters atolAuthorityParameters = buildAuthorityParametersFromRequest(req);

          if (logger.isInfoEnabled()) {
            logger.info(auditQueryParameters.toJSON());
            logger.info(atolAuthorityParameters.toJSON());
          }

          if ("users-connected".equals(type) || "users-recently-connected".equals(type)) {
            model.put("users", selectConnectedUsers(auditQueryParameters, atolAuthorityParameters));
          } else if ("users-count".equals(type)) {
            model.put("values", selectConnectedUsersByDate(auditQueryParameters));
          } else if ("users-never-connected".equals(type)) {
            model.put("users", selectNeverConnectedUsers(auditQueryParameters, atolAuthorityParameters));
          }
          model.put("type", type);
        }
      } else {
        status.setCode(Status.STATUS_UNAUTHORIZED);
      }

      return model;
    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug(e.getMessage(), e);
      }
      throw new WebScriptException("[ShareStats - SelectUsers] Error in executeImpl function");
    }
  }

  public List<String> selectConnectedUsers(AuditQueryParameters params, AtolAuthorityParameters atolAuthorityParameters) {
    // When the groups members list is empty, return an empty list without executing the query.
    if (params !=null && params.getGroupsMembers() != null && params.getGroupsMembers().isEmpty()) {
      return Collections.emptyList();
    }

    List<String> users = this.sqlSessionTemplate.selectList(SELECT_CONNECTED_USERS, params);
    Set<String> usersSet = this.selectSiteMembers(atolAuthorityParameters);
    users.retainAll(usersSet);
    return users;
  }

  public int[] selectConnectedUsersByDate(AuditQueryParameters params) {
    String[] dates = params.getSlicedDates().split(",");
    int[] values = new int[dates.length - 1];

    // When the groups members list is empty, return an empty list without executing the query.
    if (params.getGroupsMembers() != null && params.getGroupsMembers().isEmpty()) {
      return values;
    }

    for (int i = 0; i < dates.length - 1; i++) {
      params.setDateFrom(dates[i]);
      params.setDateTo(dates[i + 1]);
      List<String> users = sqlSessionTemplate.selectList(SELECT_CONNECTED_USERS, params);
      values[i] = users.size();
    }
    return values;
  }

  public Set<String> selectNeverConnectedUsers(AuditQueryParameters auditQueryParameters, AtolAuthorityParameters atolAuthorityParameters) {
    // When the groups members list is empty, return an empty HashSet without executing the query.
    if (auditQueryParameters != null && auditQueryParameters.getGroupsMembers() != null && auditQueryParameters.getGroupsMembers().isEmpty()) {
      return Collections.emptySet();
    }

    List<String> auditUsers = this.sqlSessionTemplate.selectList(SELECT_CONNECTED_USERS, auditQueryParameters);

    Set<String> usersSet;
    if (auditQueryParameters != null && auditQueryParameters.getGroupsMembers() != null) {
      usersSet = new HashSet<String>(auditQueryParameters.getGroupsMembers());
    } else {
      usersSet = this.selectSiteMembers(atolAuthorityParameters);
    }
    // Differentiel
    usersSet.removeAll(auditUsers);
    return usersSet;
  }

  public Set<String> selectSiteMembers(AtolAuthorityParameters atolAuthorityParameters) {
    // All site members
    atolAuthorityParameters.setPersonQnameId(getPersonQnameId());
    List<String> users = this.sqlSessionTemplate.selectList(SELECT_SITES_MEMBERS, atolAuthorityParameters);

    // All groups of the site
    atolAuthorityParameters.setPersonQnameId(getContainerQnameId());
    List<String> groups = this.sqlSessionTemplate.selectList(SELECT_SITES_MEMBERS, atolAuthorityParameters);

    Set<String> usersSet = new HashSet<String>(users.size());
    usersSet.addAll(users);
    for (String group : groups) {
      if (group.startsWith("GROUP_")) {
        Set<String> s = authorityService.getContainedAuthorities(AuthorityType.USER, group, false);
        usersSet.addAll(s);
      }
    }
    return usersSet;
  }

  public List<String> getGroupsMembers(String groups) {
    Set<String> groupsMembers = new HashSet<String>();
    if (groups != null) {
      String[] groupsToken = groups.split(",");
      for (String group : groupsToken) {
        if (authorityService.authorityExists(group)) {
          groupsMembers.addAll(authorityService.getContainedAuthorities(AuthorityType.USER, group, false));
        } else {
          logger.info("The group " + group + " does not exist.");
        }
      }
    }

    if (groupsMembers.isEmpty()) {
      return Collections.emptyList();
    } else {
      return new ArrayList<String>(groupsMembers);
    }
  }

  public AtolAuthorityParameters buildAuthorityParametersFromRequest(WebScriptRequest req) {
    try {
      AtolAuthorityParameters params = new AtolAuthorityParameters();
      String site = req.getParameter("site");
      String sites = req.getParameter("sites");
      if (site != null) {
        // One site
        params.setSite(site);
      } else if (!"*".equals(sites)) {
        // Several sites
        String[] sitesToken = sites.split(",");
        for (String siteId : sitesToken) {
          params.setSite(siteId);
        }
      } else {
        // Add the user's sites, plus the container group
        List<String> siteShortNames = PermissionsHelper.getUserSiteList();
        if (!siteShortNames.isEmpty()) {
          for (String siteShortName : siteShortNames) {
            params.setSite(siteShortName);
          }
        } else {
          params.setGroupNames(null);
        }
      }
      params.setMemberQnameId(this.getMemberQnameId());
      return params;
    } catch (Exception e) {
      logger.error("Error building parameters", e);
      return null;
    }
  }

  public AuditQueryParameters buildParametersFromRequest(WebScriptRequest req) {
    try {
      // Probleme de long / null
      String dateFrom = req.getParameter("from");
      String dateTo = req.getParameter("to");

      AuditQueryParameters params = new AuditQueryParameters();
      params.setSiteId(req.getParameter("site"));

      String sites = req.getParameter("sites");
      if ("*".equals(sites)) {
        params.setSitesId(PermissionsHelper.getUserSites());
      } else {
        params.setSitesId(sites);
      }

      params.setActionName(req.getParameter("action"));
      params.setAppName(req.getParameter("module"));
      params.setDateFrom(dateFrom);
      params.setDateTo(dateTo);
      params.setSlicedDates(req.getParameter("dates"));

      if (req.getParameter(PARAM_GROUPS) != null && !req.getParameter(PARAM_GROUPS).isEmpty()) {
        params.setGroupsMembers(this.getGroupsMembers(req.getParameter(PARAM_GROUPS)));
      }

      return params;
    } catch (Exception e) {
      logger.error("Error building parameters", e);
      return null;
    }
  }

  private Long getMemberQnameId() {
    if (this.memberQnameId == null) {
      this.memberQnameId = (Long) this.sqlSessionTemplate.selectOne(SELECT_QNAME_ID, "member");
    }

    return this.memberQnameId;
  }

  private Long getPersonQnameId() {
    if (this.personQnameId == null) {
      this.personQnameId = (Long) this.sqlSessionTemplate.selectOne(SELECT_QNAME_ID, "person");
    }

    return this.personQnameId;
  }

  private Long getContainerQnameId() {
    if (this.containerQnameId == null) {
      this.containerQnameId = (Long) this.sqlSessionTemplate.selectOne(SELECT_QNAME_ID, "authorityContainer");
    }

    return this.containerQnameId;
  }
}