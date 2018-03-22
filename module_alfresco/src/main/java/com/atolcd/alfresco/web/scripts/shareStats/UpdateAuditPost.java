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
package com.atolcd.alfresco.web.scripts.shareStats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.Assert;

import com.atolcd.alfresco.AuditAppEnum;
import com.atolcd.alfresco.AuditEntry;
import com.atolcd.alfresco.helper.SearchHelper;

public class UpdateAuditPost extends DeclarativeWebScript implements InitializingBean {
	// SqlMapClientTemplate for MyBatis calls
	private SqlSessionTemplate sqlSessionTemplate;
	private NodeService nodeService;
	private SiteService siteService;
	private SelectAuditsGet wsSelectAudits;

	// MyBatis query ids
	private static final String UPDATE_AUDIT_OBJECT = "alfresco.atolcd.audit.updateAuditEntry";
	private static final String MODEL_SUCCESS = "success";

	// Logger
	private static final Log logger = LogFactory.getLog(UpdateAuditPost.class);

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setWsSelectAudits(SelectAuditsGet wsSelectAudits) {
		this.wsSelectAudits = wsSelectAudits;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.sqlSessionTemplate);
		Assert.notNull(this.nodeService);
		Assert.notNull(this.siteService);
		Assert.notNull(this.wsSelectAudits);
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		Map<String, Object> model = new HashMap<String, Object>();

		// TODO: To be implemented for an administration interface

		model.put(MODEL_SUCCESS, true);
		return model;
	}

	public void updateAuditEntries() {
		List<AuditEntry> list = wsSelectAudits.selectEntriesToUpdate();
		updateAuditObjet(list);
	}

	public void updateAuditObjet(List<AuditEntry> auditEntries) {
		for (AuditEntry auditEntry : auditEntries) {
			NodeRef container = siteService.getContainer(auditEntry.getAuditSite(), auditEntry.getAuditAppName());
			if (container != null) {
				NodeRef child = getAuditNodeRef(container, auditEntry);
				if (child != null) {
					auditEntry.setAuditObject(child.toString());
					sqlSessionTemplate.update(UPDATE_AUDIT_OBJECT, auditEntry);
				}
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Audits successfully updated.");
		}
	}

	/**
	 * Retrieves NodeRef of the node audited from his container. When creating
	 * an item, it is not possible to extract his NodeRef directly.
	 * 
	 * @param container
	 *            NodeRef of the node container
	 * @param auditEntry
	 *            AuditEntry object
	 * @return NodeRef
	 */
	public NodeRef getAuditNodeRef(NodeRef container, AuditEntry auditEntry) {
		NodeRef nodeRef = null;
		NodeRef child;
		switch (AuditAppEnum.valueOf(auditEntry.getAuditAppName())) {
		case wiki:
			nodeRef = nodeService.getChildByName(container, ContentModel.ASSOC_CONTAINS, auditEntry.getAuditObject());
			break;
		case blog:
		case discussions:
			child = SearchHelper.getFirstFromQuery("+PARENT:\"" + container.toString() + "\" +@cm\\:title:\"" + auditEntry.getAuditObject() + "\"");
			if (child != null) {
				nodeRef = child;
			}
			break;
		case links:
			child = SearchHelper.getFirstFromQuery("+PARENT:\"" + container.toString() + "\" +@lnk\\:title:\"" + auditEntry.getAuditObject() + "\"");
			if (child != null) {
				nodeRef = child;
			}
			break;
		default:
			break;
		}
		return nodeRef;
	}
}