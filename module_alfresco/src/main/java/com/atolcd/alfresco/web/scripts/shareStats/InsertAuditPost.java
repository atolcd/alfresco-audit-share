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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.Assert;

import com.atolcd.alfresco.AtolVolumetryEntry;
import com.atolcd.alfresco.AuditEntry;

public class InsertAuditPost extends DeclarativeWebScript implements InitializingBean {
	private static final String INSERT_ENTRY = "alfresco.atolcd.audit.insertEntry";
	private static final String INSERT_VOLUMETRY = "alfresco.atolcd.audit.insertVolumetry";
	private static final String INSERT_VOLUMETRY_MULTI = "alfresco.atolcd.audit.insertVolumetryMulti";

	private static final String SITE_TO_FIND = "/service";
	public static final String SITE_REPOSITORY = "_repository";
	private static final String MODEL_SUCCESS = "success";

	// SqlMapClientTemplate for MyBatis calls
	private SqlSessionTemplate sqlSessionTemplate;
	private SiteService siteService;

	private static final Log logger = LogFactory.getLog(InsertAuditPost.class);

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.sqlSessionTemplate);
		Assert.notNull(this.siteService);
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		// Map that will be passed to the template
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_SUCCESS, false);
		try {
			// Check for the sqlMapClientTemplate Bean
			if (this.sqlSessionTemplate != null) {
				// Get the input content given into the request.
				String jsonArg = req.getContent().getContent();

				if (!jsonArg.isEmpty()) {
					// Fill an auditSample from the request content and insert
					// it
					AuditEntry auditSample = new AuditEntry(jsonArg);
					getSiteFromObject(auditSample);
					insert(auditSample);
					model.put(MODEL_SUCCESS, true);
				}
			}
		} catch (InvalidNodeRefException invalidNodeRefException) {
			// Node no longer exists
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e.getMessage(), e);
			}
			throw new WebScriptException("[ShareStats-DbInsert] Error in executeImpl function");
		}
		return model;
	}

	public void insert(AuditEntry auditSample) throws SQLException, JSONException {
		if (!auditSample.getAuditSite().isEmpty()) {
			sqlSessionTemplate.insert(INSERT_ENTRY, auditSample);
			logger.info("Entry successfully inserted: " + auditSample.toJSON());
		}
	}

	public void getSiteFromObject(AuditEntry auditSample) {
		// Even if we are into the repository, we try to find the site of the
		// document
		if (auditSample.getAuditSite().equals(SITE_TO_FIND)) {
			SiteInfo siteInfo = null;
			try {
				NodeRef nodeRef = new NodeRef(auditSample.getAuditObject());
				siteInfo = siteService.getSite(nodeRef);
			} catch (Exception e){
				if (logger.isDebugEnabled()) {
					logger.debug(e.getMessage(), e);
				}
			}

			if (siteInfo != null) {
				auditSample.setAuditSite(siteInfo.getShortName());
			} else {
				auditSample.setAuditSite(SITE_REPOSITORY);
			}
		}
	}

	public void insertVolumetry(AtolVolumetryEntry atolVolumetryEntry) {
		sqlSessionTemplate.insert(INSERT_VOLUMETRY, atolVolumetryEntry);
		logger.info("Volumetry entry successfully inserted.");
	}

	public void insertVolumetryMulti(List<AtolVolumetryEntry> atolVolumetryEntry) {
		sqlSessionTemplate.insert(INSERT_VOLUMETRY_MULTI, atolVolumetryEntry);
		logger.info("Volumetry entry successfully inserted.");
	}
}