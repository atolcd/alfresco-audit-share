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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
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

import com.atolcd.alfresco.AuditNodeType;
import com.atolcd.alfresco.AuditQueryParameters;
import com.atolcd.alfresco.helper.PermissionsHelper;

public class SelectNodeTypesGet extends DeclarativeWebScript implements InitializingBean {
  // Logger
  private static final Log    logger                    = LogFactory.getLog(SelectNodeTypesGet.class);

  // SqlMapClientTemplate for MyBatis calls
  private SqlSessionTemplate  sqlSessionTemplate;
  private NamespaceService    namespaceService;
  private DictionaryService   dictionaryService;

  private static final String SELECT_NODE_TYPE_BY_AUDIT = "alfresco.atolcd.audit.selectNodeType";

  public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }

  public void setNamespaceService(NamespaceService namespaceService) {
    this.namespaceService = namespaceService;
  }

  public void setDictionaryService(DictionaryService dictionaryService) {
    this.dictionaryService = dictionaryService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(this.sqlSessionTemplate);
    Assert.notNull(this.namespaceService);
    Assert.notNull(this.dictionaryService);
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

          if (logger.isInfoEnabled()) {
            logger.info(params.toJSON());
          }

          model.put("nodeTypes", selectNodeType(params, SELECT_NODE_TYPE_BY_AUDIT));
        }
      } else {
        status.setCode(Status.STATUS_UNAUTHORIZED);
      }

      return model;
    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug(e.getMessage(), e);
      }
      throw new WebScriptException("[ShareStats - SelectNodeTypes] Error in executeImpl function");
    }
  }

  public List<AuditNodeType> selectNodeType(AuditQueryParameters params, String query) {
    List<AuditNodeType> auditNodeTypeList = (List<AuditNodeType>) sqlSessionTemplate.selectList(query, params);
    logger.info("Performing " + query + " ... ");

    if (auditNodeTypeList != null && !auditNodeTypeList.isEmpty()) {
      List<AuditNodeType> res = new ArrayList<AuditNodeType>(auditNodeTypeList.size());
      for (AuditNodeType auditNodeType : auditNodeTypeList) {
        if (auditNodeType != null) {
          String myNodeType = auditNodeType.getNodeTypeValue();
          if (myNodeType != null && !myNodeType.isEmpty()) {
            try {
              QName typeQName = QName.createQName(myNodeType, this.namespaceService);
              if (Boolean.valueOf(dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT))) {
                String nodeTypeLabel = this.dictionaryService.getType(typeQName).getTitle(this.dictionaryService);
                auditNodeType.setNodeTypeLabel(nodeTypeLabel);

                res.add(auditNodeType);
              }
            } catch (Exception e) {
              logger.warn(e.getMessage(), e);
            }
          }
        }
      }
      return res;
    }

    return Collections.emptyList();
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
      return params;
    } catch (Exception e) {
      logger.error("Error building parameters", e);
      return null;
    }
  }
}
