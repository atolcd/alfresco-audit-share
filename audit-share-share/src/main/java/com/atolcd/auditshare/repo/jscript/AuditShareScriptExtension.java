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
package com.atolcd.auditshare.repo.jscript;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.alfresco.web.extensibility.SlingshotEvaluatorUtil;
import org.alfresco.web.site.SlingshotUserFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.config.RemoteConfigElement;
import org.springframework.extensions.config.RemoteConfigElement.EndpointDescriptor;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.UserFactory;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.support.AlfrescoUserFactory;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.CredentialVault;
import org.springframework.extensions.webscripts.connector.Credentials;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.processor.BaseProcessorExtension;
import org.springframework.util.Assert;

public class AuditShareScriptExtension extends BaseProcessorExtension implements InitializingBean {
  private static final Log       logger                                    = LogFactory.getLog(AuditShareScriptExtension.class);
  public static final String     AUDITSHARE_CACHE_KEY                      = "AlfAuditShareKey";
  public static final long       AUDITSHARE_CACHE_VALIDITY                 = 2 * 60 * 60 * 1000; // two hours
  public static final String     AUDITSHARE_CACHE_DATE_KEY                 = "date";
  public static final String     AUDITSHARE_CACHE_IS_MANAGER_SITE_LIST_KEY = "sites";
  public static final String     WEBSCRIPT_SITE_LIST_URL                   = "/share-stats/site/list-sites?role=SiteManager";

  private SlingshotEvaluatorUtil util                                      = null;

  public void setSlingshotEvaluatorUtil(SlingshotEvaluatorUtil slingshotExtensibilityUtil) {
    this.util = slingshotExtensibilityUtil;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(util);
  }

  public boolean isMemberOfGroups(String groupIds) {
    final RequestContext rc = ThreadLocalRequestContext.getRequestContext();
    if (rc != null) {
      if (logger.isDebugEnabled()) {
        logger.debug("Current user: " + rc.getUser().getId());
        logger.debug("Groups: " + groupIds);
        logger.debug("Site: " + this.util.getSite(rc));
      }

      List<String> groups = Arrays.asList(groupIds.split("\\s*,\\s*"));
      boolean hasMembership = this.util.isMemberOfGroups(rc, groups, true);
      if (logger.isDebugEnabled()) {
        logger.debug("hasMembership : " + hasMembership);
      }

      return hasMembership;
    }

    return false;
  }

  @SuppressWarnings("unchecked")
  public boolean isSiteMangerOfAtLeastOneSite() {
    HttpSession session = ServletUtil.getSession();
    String userName = (String) session.getAttribute(UserFactory.SESSION_ATTRIBUTE_KEY_USER_ID);

    // Note that we're ONLY caching the current user information.
    Object _cacheAuditShare = session.getAttribute(AUDITSHARE_CACHE_KEY);
    if (_cacheAuditShare instanceof JSONObject) {
      JSONObject auditShareCacheObj = (JSONObject) _cacheAuditShare;

      if (logger.isDebugEnabled()) {
        logger.debug("cache auditShareCacheObj : " + auditShareCacheObj.toString());
      }

      if (auditShareCacheObj.containsKey(AUDITSHARE_CACHE_DATE_KEY)) {
        try {
          long cacheDate = ISO8601DateFormat.parse((String) auditShareCacheObj.get(AUDITSHARE_CACHE_DATE_KEY)).getTime();
          long currentDate = System.currentTimeMillis();
          boolean useCache = (currentDate - cacheDate <= AUDITSHARE_CACHE_VALIDITY);

          if (logger.isDebugEnabled()) {
            logger.debug("cacheDate: " + cacheDate);
            logger.debug("currentDate: " + currentDate);
            logger.debug("useCache: " + useCache);
          }

          if (useCache) {
            JSONArray sites = (JSONArray) auditShareCacheObj.get(AUDITSHARE_CACHE_IS_MANAGER_SITE_LIST_KEY);
            return (sites != null && sites.size() > 0);
          }
        } catch (Exception e) {
          logger.error("Cannot read date " + AUDITSHARE_CACHE_KEY + " for user: " + userName, e);
        }
      }
    }

    try {
      final RequestContext context = ThreadLocalRequestContext.getRequestContext();
      if (context != null) {
        CredentialVault cv = context.getCredentialVault();
        if (cv != null) {
          boolean externalAuth = false;
          RemoteConfigElement config = (RemoteConfigElement) context.getServiceRegistry().getConfigService().getConfig("Remote")
              .getConfigElement("remote");
          if (config != null) {
            EndpointDescriptor descriptor = config.getEndpointDescriptor(AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID);
            if (descriptor != null) {
              externalAuth = descriptor.getExternalAuth();
            }
          }

          Credentials creds = cv.retrieve(AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID);
          // MNT-11857 - Check for external authentication
          if (creds == null && !externalAuth) {
            // User is not logged in anymore
            return false;
          }

          Connector connector = context.getServiceRegistry().getConnectorService().getConnector(SlingshotUserFactory.ALFRESCO_ENDPOINT_ID,
              userName, ServletUtil.getSession());

          if (logger.isDebugEnabled()) {
            logger.debug("calling webscript: " + WEBSCRIPT_SITE_LIST_URL);
          }

          Response res = connector.call(WEBSCRIPT_SITE_LIST_URL);
          if (res.getStatus().getCode() == Status.STATUS_OK) {
            String response = res.getResponse();
            if (logger.isDebugEnabled()) {
              logger.debug("webscript response: " + response);
            }

            Object jsonRes = new JSONParser().parse(response);
            if (jsonRes instanceof JSONArray) {
              JSONArray sites = (JSONArray) jsonRes;

              JSONObject auditShareCacheObj = new JSONObject();
              auditShareCacheObj.put(AUDITSHARE_CACHE_DATE_KEY, ISO8601DateFormat.format(new Date()));
              auditShareCacheObj.put(AUDITSHARE_CACHE_IS_MANAGER_SITE_LIST_KEY, sites);

              session.setAttribute(AUDITSHARE_CACHE_KEY, auditShareCacheObj);

              if (logger.isDebugEnabled()) {
                logger.debug("webscript auditShareCacheObj : " + auditShareCacheObj.toString());
              }

              return (sites != null && sites.size() > 0);
            }
          }
        }
      }
    } catch (ConnectorServiceException e) {
      logger.error(e.getMessage(), e);
    } catch (ParseException e) {
      logger.error(e.getMessage(), e);
    }

    return false;
  }
}
