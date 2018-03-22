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
package com.atolcd.alfresco.scheduler;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.atolcd.alfresco.repo.jscript.ShareStats;
import com.atolcd.alfresco.web.scripts.shareStats.InsertAuditPost;

public class SiteVolumetryJobExecuter {
  @SuppressWarnings("unused")
  private static final Log logger = LogFactory.getLog(SiteVolumetryJobExecuter.class);

  private InsertAuditPost  wsInsertAudits;
  protected SiteService    siteService;
  protected SearchService  searchService;
  protected NodeService    nodeService;
  protected int            batchSize;

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

  public void setWsInsertAudits(InsertAuditPost wsInsertAudits) {
    this.wsInsertAudits = wsInsertAudits;
  }

  public int getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  public void execute() {
    ShareStats shareStats = new ShareStats();
    shareStats.setSearchService(searchService);
    shareStats.setWsInsertAudits(wsInsertAudits);
    shareStats.setSiteService(siteService);
    shareStats.setNodeService(nodeService);
    shareStats.setBatchSize(batchSize);
    shareStats.insertVolumetryMulti(new Date().getTime());
  }
}
