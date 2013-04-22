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
	// SqlMapClientTemplate for ibatis calls
	private SqlSessionTemplate sqlSessionTemplate;
	private NodeService nodeService;
	private SiteService siteService;
	private SelectAuditsGet wsSelectAudits;

	// Identifiant de la requête côté iBatis
	private static final String UPDATE_AUDIT_OBJECT = "alfresco.atolcd.audit.updateAuditEntry";
	private static final String MSG_OK = "La mise à jour des audits s'est déroulée correctement.";
	private static final String MODEL_SUCCESS = "success";

	// logger
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

		// TODO : A implémenter avec une interface administration

		model.put(MODEL_SUCCESS, true);
		return model;
	}

	/**
	 * Mise à jour des objets des audits
	 */
	public void updateAuditEntries() {
		List<AuditEntry> list = wsSelectAudits.selectEntriesToUpdate();
		updateAuditObjet(list);
	}

	/**
	 * Met à jour un audit en récupérant le nodeRef
	 * 
	 * @param auditEntries
	 */
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
			logger.debug(MSG_OK);
		}
	}

	/**
	 * Récupère le nodeRef de l'audit dans le container "conteneur". Lors de la
	 * création d'un élément, il n'est pas possible d'extraire le nodeRef
	 * directement.
	 * 
	 * @param container
	 * @param auditEntry
	 * @return
	 */
	public NodeRef getAuditNodeRef(NodeRef container, AuditEntry auditEntry) {
		NodeRef nodeRef = null, child = null;
		switch (AuditAppEnum.valueOf(auditEntry.getAuditAppName())) {
		case wiki:
			nodeRef = nodeService.getChildByName(container, ContentModel.ASSOC_CONTAINS, auditEntry.getAuditObject());
			break;
		case blog:
		case discussions:
			child = SearchHelper.getFirstFromQuery("+PARENT:\"" + container.toString() + "\" +@cm\\:title:\"" + auditEntry.getAuditObject()
					+ "\"");
			if (child != null) {
				nodeRef = child;
			}
			break;
		case links:
			child = SearchHelper.getFirstFromQuery("+PARENT:\"" + container.toString() + "\" +@lnk\\:title:\""
					+ auditEntry.getAuditObject() + "\"");
			if (child != null) {
				nodeRef = child;
			}
			break;
		default:
			// TODO : Traiter les autres cas si besoin
			break;
		}
		return nodeRef;
	}
}
