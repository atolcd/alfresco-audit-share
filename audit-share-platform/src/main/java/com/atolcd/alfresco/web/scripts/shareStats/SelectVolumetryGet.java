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
package com.atolcd.alfresco.web.scripts.shareStats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.atolcd.alfresco.AuditQueryParameters;
import com.atolcd.alfresco.helper.PermissionsHelper;

public class SelectVolumetryGet extends DeclarativeWebScript implements InitializingBean {
  // Logger
  private static final Log    logger                 = LogFactory.getLog(SelectVolumetryGet.class);

  // SqlMapClientTemplate for MyBatis calls
  private SqlSessionTemplate  sqlSessionTemplate;
  private SiteService         siteService;

  private static final String SELECT_VOLUMETRY       = "alfresco.atolcd.audit.selectVolumetry";

  private static final String SELECT_VOLUMETRY_SITES = "alfresco.atolcd.audit.special-queries.selectVolumetrySites";

  public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }

  public void setSiteService(SiteService siteService) {
    this.siteService = siteService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(sqlSessionTemplate, "There must be a sqlSessionTemplate");
    Assert.notNull(siteService, "There must be a siteService");
  }

  @Override
  protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
    try {
      Long calculateTime = System.currentTimeMillis();
      Map<String, Object> model = new HashMap<>();
      if (PermissionsHelper.isAuthorized(req)) {
        if (this.sqlSessionTemplate != null) {
          AuditQueryParameters params = buildParametersFromRequest(req);

          if (logger.isInfoEnabled()) {
            logger.info(params.toJSON());
          }

          String[] dates = params.getSlicedDates().split(",");
          boolean sendStackedValues = Boolean.parseBoolean(req.getParameter("stacked"));
          Map<String, List<Long>> stackedValues = new HashMap<>(dates.length - 1);
          List<Long> countValues = new ArrayList<>(dates.length - 1);

          List<String> siteIds = new ArrayList<>();
          String requete = SELECT_VOLUMETRY_SITES;

          if (params.getSiteId() != null) {
            siteIds.add(params.getSiteId());
            requete = SELECT_VOLUMETRY;
          } else if (params.getSitesId() != null) {
            siteIds = params.getSitesId();
          } else {
            siteIds = PermissionsHelper.getUserSiteList();
          }

          if (sendStackedValues) {
            // Site by site
            params.setSitesId(Collections.<String> emptyList());
          } else {
            params.setSitesId(siteIds);
          }

          for (int i = 0; i < dates.length - 1; i++) {
            params.setDateFrom(dates[i]);
            params.setDateTo(dates[i + 1]);

            List<Long> values = new ArrayList<>(siteIds.size());
            Long total = null;

            if (logger.isTraceEnabled()) {
              logger.trace(" before :" + (System.currentTimeMillis() - calculateTime));
            }

            if (sendStackedValues) {
              for (String site : siteIds) {
                params.setSitesId(site);
                Object o = sqlSessionTemplate.selectOne(SELECT_VOLUMETRY, params);
                if (o == null) {
                  values.add(null);
                } else {
                  values.add((Long) o);

                  if (total == null) {
                    total = (Long) o;
                  } else {
                    total += (Long) o;
                  }
                }
              }
            } else {
              Object o = sqlSessionTemplate.selectOne(requete, params);
              if (o == null) {
                values.add(null);
              } else {
                values.add((Long) o);
                total = (Long) o;
              }
            }

            if (logger.isTraceEnabled()) {
              logger.trace(" after :" + (System.currentTimeMillis() - calculateTime));
            }

            countValues.add(total);
            if (sendStackedValues) {
              stackedValues.put(String.valueOf(i > 9 ? i : "0" + i), values);
            }
          }

          if (sendStackedValues) {
            model.put("stackedValues", stackedValues.entrySet());
          }
          model.put("values", countValues);
          model.put("sites", siteIds);
        }
      } else {
        status.setCode(Status.STATUS_UNAUTHORIZED);
      }

      return model;
    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug(e.getMessage(), e);
      }
      throw new WebScriptException("[ShareStats - SelectVolumetry] Error in executeImpl function");
    }
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
