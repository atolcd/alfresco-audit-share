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
package com.atolcd.alfresco.repo.jscript;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.ContentDataWithId;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.ISO9075;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.atolcd.alfresco.AtolVolumetryEntry;
import com.atolcd.alfresco.AuditCount;
import com.atolcd.alfresco.AuditEntry;
import com.atolcd.alfresco.AuditQueryParameters;
import com.atolcd.alfresco.web.scripts.shareStats.InsertAuditPost;
import com.atolcd.alfresco.web.scripts.shareStats.SelectAuditsGet;
import com.atolcd.auditshare.repo.service.AuditShareReferentielService;
import com.atolcd.auditshare.repo.xml.Group;

public class ShareStats extends BaseScopableProcessorExtension implements InitializingBean {
  // Logger
  private static final Log             logger = LogFactory.getLog(ShareStats.class);

  private InsertAuditPost              wsInsertAudits;
  private SiteService                  siteService;
  private SearchService                searchService;
  private int                          batchSize;
  // SqlMapClientTemplate for MyBatis calls
  private SqlSessionTemplate           sqlSessionTemplate;

  // For the user groups referentiel services
  private AuditShareReferentielService auditShareReferentielService;

  public SiteService getSiteService() {
    return siteService;
  }

  public void setSiteService(SiteService siteService) {
    this.siteService = siteService;
  }

  public SearchService getSearchService() {
    return searchService;
  }

  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

  public int getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  public void setWsInsertAudits(InsertAuditPost wsInsertAudits) {
    this.wsInsertAudits = wsInsertAudits;
  }

  public AuditShareReferentielService getAuditShareReferentielService() {
    return auditShareReferentielService;
  }

  public void setAuditShareReferentielService(AuditShareReferentielService auditShareReferentielService) {
    this.auditShareReferentielService = auditShareReferentielService;
  }

  public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(siteService, "siteService must not be null");
    Assert.notNull(searchService, "searchService must not be null");
    Assert.notNull(wsInsertAudits, "wsInsertAudits must not be null");
    Assert.notNull(auditShareReferentielService, "auditShareReferentielService must not be null");
    Assert.notNull(sqlSessionTemplate, "sqlSessionTemplate must not be null");
  }

  public List<Group> getReferentiel(String refGroup) {
    // Referentiel
    return auditShareReferentielService.parseRefentielForNodeUUID(refGroup);
  }

  public void insertAuditEntry(long id, String auditUserId, String auditSite, String auditAppName, String auditActionName,
      String auditObject, long auditTime, String auditNodeType) throws SQLException, JSONException {
    AuditEntry auditSample = new AuditEntry();
    auditSample.setId(id);
    auditSample.setAuditUserId(auditUserId);
    auditSample.setAuditAppName(auditAppName);
    auditSample.setAuditActionName(auditActionName);
    auditSample.setAuditObject(auditObject);
    auditSample.setAuditTime(auditTime);

    auditSample.setAuditSite(StringUtils.isNotBlank(auditSite) ? auditSite : InsertAuditPost.SITE_REPOSITORY);

    if (StringUtils.isNotBlank(auditNodeType)) {
      auditSample.setAuditNodeType(auditNodeType);
    }

    sqlSessionTemplate.insert(InsertAuditPost.INSERT_ENTRY, auditSample);
    logger.info("Entry successfully inserted: " + auditSample.toJSON());
  }

  public List<AuditCount> selectByRead(String auditAppNames, String auditActionNames, String auditUserIds, String auditSites,
      String auditObject, Long dateFrom, Long dateTo, String auditNodeTypes) throws SQLException, JSONException {
    return this.selectAuditCount(SelectAuditsGet.SELECT_BY_VIEW, auditAppNames, auditActionNames, auditUserIds, auditSites, auditObject,
        dateFrom, dateTo, auditNodeTypes);
  }

  public List<AuditCount> selectByCreated(String auditAppNames, String auditActionNames, String auditUserIds, String auditSites,
      String auditObject, Long dateFrom, Long dateTo, String auditNodeTypes) throws SQLException, JSONException {
    return this.selectAuditCount(SelectAuditsGet.SELECT_BY_CREATED, auditAppNames, auditActionNames, auditUserIds, auditSites, auditObject,
        dateFrom, dateTo, auditNodeTypes);
  }

  public List<AuditCount> selectByUpdated(String auditAppNames, String auditActionNames, String auditUserIds, String auditSites,
      String auditObject, Long dateFrom, Long dateTo, String auditNodeTypes) throws SQLException, JSONException {
    return this.selectAuditCount(SelectAuditsGet.SELECT_BY_UPDATED, auditAppNames, auditActionNames, auditUserIds, auditSites, auditObject,
        dateFrom, dateTo, auditNodeTypes);
  }

  public List<AuditCount> selectByDeleted(String auditAppNames, String auditActionNames, String auditUserIds, String auditSites,
      String auditObject, Long dateFrom, Long dateTo, String auditNodeTypes) throws SQLException, JSONException {
    return this.selectAuditCount(SelectAuditsGet.SELECT_BY_DELETED, auditAppNames, auditActionNames, auditUserIds, auditSites, auditObject,
        dateFrom, dateTo, auditNodeTypes);
  }

  private List<AuditCount> selectAuditCount(String queryType, String auditAppNames, String auditActionNames, String auditUserIds,
      String auditSites, String auditObject, Long dateFrom, Long dateTo, String auditNodeTypes) throws SQLException, JSONException {
    AuditQueryParameters params = new AuditQueryParameters();

    params.setAppNames(auditAppNames);
    params.setActionNames(auditActionNames);
    params.setUserIds(auditUserIds);
    params.setSitesId(auditSites);
    params.setObject(auditObject);
    if (dateFrom != null) {
      params.setDateFrom(dateFrom);
    }
    if (dateTo != null) {
      params.setDateTo(dateTo);
    }
    params.setNodeTypes(auditNodeTypes);

    logger.info("AuditQueryParameters: " + params.toJSON());

    return sqlSessionTemplate.selectList(queryType, params);
  }

  public boolean insertVolumetry(String siteId, long siteSize, int folderCount, int fileCount, long atTime) {
    boolean success = true;
    try {
      AtolVolumetryEntry atolVolumetryEntry = new AtolVolumetryEntry(siteId, siteSize, folderCount, fileCount, atTime);
      this.wsInsertAudits.insertVolumetry(atolVolumetryEntry);
    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug(e.getMessage(), e);
      }
      success = false;
    }
    return success;
  }

  public boolean insertVolumetryMulti(long atTime) {
    boolean success = true;
    try {
      this.wsInsertAudits.insertVolumetryMulti(getDocsCmis(atTime));
    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug(e.getMessage(), e);
      }
      success = false;
    }
    return success;
  }

  public List<AtolVolumetryEntry> getDocsCmis(long atTime) {
    List<SiteInfo> siteInfos = siteService.findSites(null, 0);
    SearchParameters searchParameters = new SearchParameters();
    searchParameters.setLanguage(SearchService.LANGUAGE_CMIS_ALFRESCO);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setLimit(0);
    searchParameters.setLimitBy(LimitBy.UNLIMITED);
    searchParameters.setMaxPermissionChecks(Integer.MAX_VALUE);
    searchParameters.setMaxPermissionCheckTimeMillis(Long.MAX_VALUE);
    searchParameters.setMaxItems(batchSize);
    long totalSize = 0;
    List<AtolVolumetryEntry> listAtolVolEntry = new ArrayList<>();
    for (SiteInfo siteInfo : siteInfos) {
      AtolVolumetryEntry atolVolumetryEntry = getTotalSizeSite(searchParameters, siteInfo, atTime);
      listAtolVolEntry.add(atolVolumetryEntry);
      totalSize = totalSize + atolVolumetryEntry.getSiteSize();
    }
    return listAtolVolEntry;
  }

  private AtolVolumetryEntry getTotalSizeSite(SearchParameters searchParameters, SiteInfo siteInfo, long atTime) {
    String query = "SELECT * FROM cmis:document D WHERE CONTAINS(D,'PATH: \"/app:company_home/st:sites/cm:"
        + ISO9075.encode(siteInfo.getShortName()) + "//*\"')";
    searchParameters.setQuery(query);

    long totalSize = 0;
    int nbFile = 0;
    int skipCount = 0;

    while (true) {
      searchParameters.setSkipCount(skipCount);
      ResultSet rs = null;
      try {
        rs = searchService.query(searchParameters);

        if (rs.length() == 0) {
          // we are at the end of our search, no more results are available
          break;
        }

        for (ResultSetRow rsr : rs) {
          // Useful to avoid nullpointer at the following line
          @SuppressWarnings("unused")
          Map<String, Serializable> mapValue = rsr.getValues();
          ContentDataWithId docContent = (ContentDataWithId) rsr.getValue(ContentModel.PROP_CONTENT);
          if (docContent != null) {
            totalSize = totalSize + docContent.getSize();
            nbFile++;
          }
        }
        skipCount += rs.length();

        if (logger.isDebugEnabled()) {
          logger.debug("  -- Site Volumetry " + siteInfo.getShortName() + ": " + skipCount);
        }
      } finally {
        if (rs != null) {
          rs.close();
        }
      }
    }
    return new AtolVolumetryEntry(siteInfo.getShortName(), totalSize, 0, nbFile, atTime);
  }
}
