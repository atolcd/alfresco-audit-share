package com.atolcd.alfresco.scheduler;

import java.util.Date;

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
    shareStats.setBatchSize(batchSize);
    shareStats.insertVolumetryMulti(new Date().getTime());
  }
}
