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
package com.atolcd.alfresco.web.scripts.shareStats;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.Assert;

import com.atolcd.alfresco.AuditCount;
import com.atolcd.alfresco.AuditEntry;
import com.atolcd.alfresco.AuditObjectPopularity;
import com.atolcd.alfresco.AuditQueryParameters;
import com.atolcd.alfresco.helper.PermissionsHelper;

public class SelectAuditsGet extends DeclarativeWebScript implements InitializingBean {
  // Logger
  private static final Log logger = LogFactory.getLog(SelectAuditsGet.class);

  // SqlMapClientTemplate for MyBatis calls
  private SqlSessionTemplate sqlSessionTemplate;
  private NodeService nodeService;
  private SiteService siteService;
  private int limitMostReadOrUpdate;

  public static final String SELECT_BY_VIEW = "alfresco.atolcd.audit.selectByRead";
  public static final String SELECT_BY_CREATED = "alfresco.atolcd.audit.selectByCreated";
  public static final String SELECT_BY_UPDATED = "alfresco.atolcd.audit.selectByUpdated";
  public static final String SELECT_BY_DELETED = "alfresco.atolcd.audit.selectByDeleted";
  public static final String SELECT_BY_MOSTREAD = "alfresco.atolcd.audit.special-queries.selectByMostRead";
  public static final String SELECT_BY_MOSTUPDATED = "alfresco.atolcd.audit.special-queries.selectByMostUpdated";
  public static final String SELECT_TO_UPDATE = "alfresco.atolcd.audit.selectEntriesToUpdate";

  static final QName TYPE_DATALIST = QName.createQName("http://www.alfresco.org/model/datalist/1.0", "dataList");
  static final QName TYPE_CALENDAR_EVENT = QName.createQName("http://www.alfresco.org/model/calendar", "calendarEvent");
  static final QName PROP_CALENDAR_EVENT_WHAT = QName.createQName("http://www.alfresco.org/model/calendar", "whatEvent");
  static final QName TYPE_LINK = QName.createQName("http://www.alfresco.org/model/linksmodel/1.0", "link");
  static final QName PROP_LINK_TITLE = QName.createQName("http://www.alfresco.org/model/linksmodel/1.0", "title");

  static final String MODEL_DATES_VARIABLE = "dates";
  static final String MODEL_POPULARITY_VARIABLE = "popularity";

  public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setSiteService(SiteService siteService) {
    this.siteService = siteService;
  }

  public int getLimitMostReadOrUpdate() {
    return limitMostReadOrUpdate;
  }

  public void setLimitMostReadOrUpdate(int limitMostReadOrUpdate) {
    this.limitMostReadOrUpdate = limitMostReadOrUpdate;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(this.sqlSessionTemplate);
    Assert.notNull(this.nodeService);
  }

  @Override
  protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
    try {
      Map<String, Object> model = new HashMap<String, Object>();
      if (PermissionsHelper.isAuthorized(req)) {
        // Check for the sqlMapClientTemplate Bean
        if (this.sqlSessionTemplate != null) {
          // Get the input content given into the request.
          AuditQueryParameters params = buildParametersFromRequest(req);
          String type = req.getParameter("type");
          String stringLimit = req.getParameter("limit");

          //Check if limit exist
          if (stringLimit != null && !stringLimit.isEmpty()) {
            this.limitMostReadOrUpdate = Integer.parseInt(stringLimit);
          }

          params.setLimit(limitMostReadOrUpdate);

          if (logger.isInfoEnabled()) {
            logger.info(params.toJSON());
          }

          checkForQuery(model, params, type);
        }
      } else {
        status.setCode(Status.STATUS_UNAUTHORIZED);
      }

      return model;
    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug(e.getMessage(), e);
      }
      throw new WebScriptException("[ShareStats - SelectAudits] Error in executeImpl function");
    }
  }

  public void checkForQuery(Map<String, Object> model, AuditQueryParameters params, String type) throws SQLException,
      JSONException {
    switch (queryType.valueOf(type)) {
    case read:
      model.put(MODEL_DATES_VARIABLE, selectByDate(params, SELECT_BY_VIEW));
      break;
    case created:
      model.put(MODEL_DATES_VARIABLE, selectByDate(params, SELECT_BY_CREATED));
      break;
    case deleted:
      model.put(MODEL_DATES_VARIABLE, selectByDate(params, SELECT_BY_DELETED));
      break;
    case updated:
      model.put(MODEL_DATES_VARIABLE, selectByDate(params, SELECT_BY_UPDATED));
      break;
    case mostread:
      model.put(MODEL_POPULARITY_VARIABLE, selectByPopularity(params, SELECT_BY_MOSTREAD));
      break;
    case mostupdated:
    default:
      model.put(MODEL_POPULARITY_VARIABLE, selectByPopularity(params, SELECT_BY_MOSTUPDATED));
      break;
    }
  }

  @SuppressWarnings("unchecked")
  public List<AuditObjectPopularity> selectByPopularity(AuditQueryParameters params, String query) {
    List<AuditObjectPopularity> auditObjectPopularityList = (List<AuditObjectPopularity>) sqlSessionTemplate.selectList(query, params);
    logger.info("Performing " + query + " ... ");

    Iterator<AuditObjectPopularity> iterator = auditObjectPopularityList.iterator();
    // Verify if the returned items always exist
    while (iterator.hasNext()) {
      AuditObjectPopularity auditObjectPopularity = iterator.next();
      try {
        NodeRef nodeRef = new NodeRef(auditObjectPopularity.getAuditObject());
        if (!nodeService.exists(nodeRef)) {
          iterator.remove();
        } else {
          auditObjectPopularity.setObjectName((String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
          auditObjectPopularity.setObjectDisplayName(getPrettyDisplayname(nodeRef));

          SiteInfo si = siteService.getSite(nodeRef);
          if (si != null) {
            // Find in which site component is this node
            Path nodePath = nodeService.getPath(nodeRef);
            if (nodePath.size() > 4) {
              String siteContainerQName = nodeService.getPath(nodeRef).get(4).getElementString();
              auditObjectPopularity.setSiteComponent(QName.createQName(siteContainerQName).getLocalName());
            }
          }

        }
      } catch (AlfrescoRuntimeException e) {
        iterator.remove();
        logger.warn(e.getMessage(), e);
      }
    }

    return auditObjectPopularityList;
  }

  @SuppressWarnings("unchecked")
  public List<List<AuditCount>> selectByDate(AuditQueryParameters params, String query) {
    String[] dates = params.getSlicedDates().split(",");
    List<List<AuditCount>> auditCount = new ArrayList<List<AuditCount>>();
    for (int i = 0; i < dates.length - 1; i++) {
      params.setDateFrom(dates[i]);
      params.setDateTo(dates[i + 1]);
      List<AuditCount> auditSample = (List<AuditCount>) sqlSessionTemplate.selectList(query, params);
      auditCount.add(auditSample);
    }
    logger.info("Performing " + query + " ... ");
    return auditCount;
  }

  @SuppressWarnings("unchecked")
  public List<AuditEntry> selectEntriesToUpdate() {
    return (List<AuditEntry>) sqlSessionTemplate.selectList(SELECT_TO_UPDATE);
  }

  public AuditQueryParameters buildParametersFromRequest(WebScriptRequest req) {
    try {
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
      params.setAppNames(req.getParameter("modules"));
      params.setDateFrom(dateFrom);
      params.setDateTo(dateTo);
      params.setSlicedDates(req.getParameter("dates"));
      params.setNodeType(req.getParameter("nodeType"));
      params.setNodeTypes(req.getParameter("nodeTypes"));
      return params;
    } catch (Exception e) {
      logger.error("Error building parameters", e);
      return null;
    }
  }

  private String getPrettyDisplayname(NodeRef nodeRef) {
    String nodeName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

    QName nodeType = nodeService.getType(nodeRef);
    if (nodeType.equals(TYPE_DATALIST)) {
      // DataList: use title
      return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
    } else if (nodeType.equals(ForumModel.TYPE_TOPIC)) {
      // Discussion: find first child that have the same name
      NodeRef firstTopic = nodeService.getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS, nodeName);
      if (firstTopic != null) {
        return (String) nodeService.getProperty(firstTopic, ContentModel.PROP_TITLE);
      }
    } else if (nodeType.equals(TYPE_LINK)) {
      // Link: use link title
      return (String) nodeService.getProperty(nodeRef, PROP_LINK_TITLE);
    } else if (nodeType.equals(TYPE_CALENDAR_EVENT)) {
      // Event: use 'what' metadata
      return (String) nodeService.getProperty(nodeRef, PROP_CALENDAR_EVENT_WHAT);
    } else {
      // Others: content, wiki, blog
      NodeRef parentRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
      if (parentRef != null) {
        if (nodeService.hasAspect(parentRef, SiteModel.ASPECT_SITE_CONTAINER)) {
          String parentName = (String) nodeService.getProperty(parentRef, ContentModel.PROP_NAME);
          if ("blog".equals(parentName) || "wiki".equals(parentName)) {
            // For Blog or Wiki pages, we use the title
            return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
          }
        }
      }
    }

    return nodeName;
  }
}