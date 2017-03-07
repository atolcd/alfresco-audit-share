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
package com.atolcd.alfresco.repo.jscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.ContentDataWithId;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.atolcd.alfresco.AtolVolumetryEntry;
import com.atolcd.alfresco.web.scripts.shareStats.InsertAuditPost;
import com.atolcd.auditshare.repo.service.AuditShareReferentielService;
import com.atolcd.auditshare.repo.xml.Group;

public class ShareStats extends BaseScopableProcessorExtension implements InitializingBean {
  // Logger
  private static final Log   logger = LogFactory.getLog(ShareStats.class);

  private InsertAuditPost    wsInsertAudits;
  private SiteService        siteService;
  private SearchService      searchService;
  private NodeService        nodeService;
  protected int              batchSize;

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

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
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

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(siteService);
    Assert.notNull(searchService);
    Assert.notNull(nodeService);
    Assert.notNull(wsInsertAudits);
    Assert.notNull(auditShareReferentielService);
  }

  public List<Group> getReferentiel(String refGroup) {
    // Referentiel
    return auditShareReferentielService.parseRefentielForNodeUUID(refGroup);
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
    List<SiteInfo> siteInfos = siteService.findSites(null, null, 0);
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
    String query = "SELECT * FROM cmis:document D WHERE CONTAINS(D,'PATH: \"/app:company_home/st:sites/cm:" + siteInfo.getShortName()
        + "//*\"')";
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
          try {
            // Useful to avoid nullpointer at the following line
            @SuppressWarnings("unused")
            Map<String, Serializable> mapValue = rsr.getValues();
            ContentDataWithId docContent = (ContentDataWithId) rsr.getValue(ContentModel.PROP_CONTENT);
            if (docContent != null) {
              totalSize = totalSize + docContent.getSize();
              nbFile++;
            }
          } catch (UnsupportedOperationException uoe) {
            NodeRef nodeRef = rsr.getNodeRef();
            ContentData docContent = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
            if (docContent != null) {
              totalSize = totalSize + docContent.getSize();
              nbFile++;
            }
          }
        }
        skipCount += rs.length();

        if (logger.isDebugEnabled()) {
          logger.debug("  -- Site Volumetry " + siteInfo.getShortName() + ": " + skipCount);
        }
      } catch (Exception e) {
        logger.error("An error occurred while calculating total site size.", e);
      } finally {
        if (rs != null) {
          rs.close();
        }
      }
    }
    return new AtolVolumetryEntry(siteInfo.getShortName(), totalSize, 0, nbFile, atTime);
  }
}
