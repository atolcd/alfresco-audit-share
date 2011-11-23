package com.atolcd.alfresco.web.scripts.shareStats;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.orm.ibatis.SqlMapClientTemplate;
import org.springframework.util.Assert;

import com.atolcd.alfresco.AuditEntry;

public class InsertAuditPost extends DeclarativeWebScript implements InitializingBean {
	private static final String INSERT_ENTRY = "alfresco.atolcd.audit.insertEntry";
	// SqlMapClientTemplate for ibatis calls
	private SqlMapClientTemplate sqlMapClientTemplate;
	private SiteService siteService;

	private static final Log logger = LogFactory.getLog(InsertAuditPost.class);

	public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate) {
		this.sqlMapClientTemplate = sqlMapClientTemplate;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.sqlMapClientTemplate);
		Assert.notNull(this.siteService);
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		try {
			// Map that will be passed to the template
			Map<String, Object> model = new HashMap<String, Object>();

			// Check for the sqlMapClientTemplate Bean
			if (this.sqlMapClientTemplate != null) {
				// Get the input content given into the request.
				String jsonArg = req.getContent().getContent();

				if (!jsonArg.isEmpty()) {
					// Fill an auditSample from the request content and insert
					// it
					AuditEntry auditSample = new AuditEntry(jsonArg);
					getSiteFromObject(auditSample);
					insert(auditSample);
					model.put("success", true);
				} else {
					model.put("success", false);
				}
			}
			return model;
		} catch (Exception e) {
			throw new WebScriptException("[ShareStats-DbInsert] Error in executeImpl function");
		}
	}

	public void insert(AuditEntry auditSample) throws SQLException, JSONException {
		if (!auditSample.getAuditSite().isEmpty()) {
			sqlMapClientTemplate.insert(INSERT_ENTRY, auditSample);
			logger.info("Insert ok : " + auditSample.toJSON());
		}
	}

	public void getSiteFromObject(AuditEntry auditSample) {
		if (auditSample.getAuditSite().equals("/service")) {
			NodeRef nodeRef = new NodeRef(auditSample.getAuditObject());
			SiteInfo siteInfo = siteService.getSite(nodeRef);
			if (siteInfo != null) {
				auditSample.setAuditSite(siteInfo.getShortName());
			} else {
				auditSample.setAuditSite("");
			}
		}

	}
}
