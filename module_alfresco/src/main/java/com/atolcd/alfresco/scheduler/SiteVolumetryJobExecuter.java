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

  public void execute() {
    ShareStats shareStats = new ShareStats();
    shareStats.setSearchService(searchService);
    shareStats.setWsInsertAudits(wsInsertAudits);
    shareStats.setSiteService(siteService);
    shareStats.setNodeService(nodeService);
    shareStats.insertVolumetryMulti(new Date().getTime());
  }
}
