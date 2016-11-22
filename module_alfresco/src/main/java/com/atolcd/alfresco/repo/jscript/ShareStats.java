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

public class ShareStats extends BaseScopableProcessorExtension implements InitializingBean {
  // Logger
  private static final Log logger = LogFactory.getLog(ShareStats.class);

  private InsertAuditPost  wsInsertAudits;
  private SiteService      siteService;
  private SearchService    searchService;
  private NodeService      nodeService;

  public void setSiteService(SiteService siteService) {
    this.siteService = siteService;
  }

  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setWsInsertAudits(InsertAuditPost wsInsertAudits) {
    this.wsInsertAudits = wsInsertAudits;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(siteService);
    Assert.notNull(searchService);
    Assert.notNull(nodeService);
    Assert.notNull(wsInsertAudits);
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
    searchParameters.setMaxItems(-1);
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
    ResultSet rs = null;
    long totalSize = 0;
    int nbFile = 0;
    try {
      rs = searchService.query(searchParameters);

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
    } catch (Exception e) {
      logger.error("An error occurred while calculating total site size.", e);
    } finally {
      if (rs != null) {
        rs.close();
      }
    }
    return new AtolVolumetryEntry(siteInfo.getShortName(), totalSize, 0, nbFile, atTime);
  }
}
