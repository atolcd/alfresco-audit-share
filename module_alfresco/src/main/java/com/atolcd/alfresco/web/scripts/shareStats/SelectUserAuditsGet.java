package com.atolcd.alfresco.web.scripts.shareStats;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
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

import com.atolcd.alfresco.AuditCount;
import com.atolcd.alfresco.AuditEntry;
import com.atolcd.alfresco.AuditObjectPopularity;
import com.atolcd.alfresco.AuditQueryParameters;
import com.atolcd.alfresco.helper.AverageComparator;

public class SelectUserAuditsGet extends DeclarativeWebScript implements InitializingBean {
	private static final Log logger = LogFactory.getLog(SelectUserAuditsGet.class);
	private SqlSessionTemplate sqlSessionTemplate;
	private NodeService nodeService;
	private SiteService siteService;
	private PermissionService permissionService;
	private String currentUser;
	private static final String SELECT_BY_VIEW = "alfresco.atolcd.audit.selectByRead";
	private static final String SELECT_BY_CREATED = "alfresco.atolcd.audit.selectByCreated";
	private static final String SELECT_BY_UPDATED = "alfresco.atolcd.audit.selectByUpdated";
	private static final String SELECT_BY_DELETED = "alfresco.atolcd.audit.selectByDeleted";
	private static final String SELECT_BY_MOSTREAD = "alfresco.atolcd.audit.selectByMostRead";
	private static final String SELECT_BY_MOSTUPDATED = "alfresco.atolcd.audit.selectByMostUpdated";
	private static final String SELECT_TO_UPDATE = "alfresco.atolcd.audit.selectEntriesToUpdate";
	private static final String SELECT_BY_USERMOSTREAD = "alfresco.atolcd.audit.selectByUserMostRead";
	
	static final QName TYPE_DATALIST = QName.createQName("http://www.alfresco.org/model/datalist/1.0", "dataList");
	static final QName TYPE_CALENDAR_EVENT = QName.createQName("http://www.alfresco.org/model/calendar",
			"calendarEvent");
	static final QName PROP_CALENDAR_EVENT_WHAT = QName.createQName("http://www.alfresco.org/model/calendar",
			"whatEvent");
	static final QName TYPE_LINK = QName.createQName("http://www.alfresco.org/model/linksmodel/1.0", "link");
	static final QName PROP_LINK_TITLE = QName.createQName("http://www.alfresco.org/model/linksmodel/1.0", "title");

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public String getCurrentUser() {
		return this.currentUser;
	}

	public void setCurrentUser(String currentUser) {
		this.currentUser = currentUser;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.sqlSessionTemplate);
		Assert.notNull(this.nodeService);
	}

	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		try {
			Map<String, Object> model = new HashMap();

			setCurrentUser(AuthenticationUtil.getRunAsUser());
			if (this.sqlSessionTemplate != null) {
				AuditQueryParameters params = buildParametersFromRequest(req);
				String type = req.getParameter("type");
				String stringLimit = req.getParameter("limit");
				int limit = 0;
				if ((stringLimit != null) && (!stringLimit.isEmpty())) {
					limit = Integer.parseInt(stringLimit);
				}
				checkForQuery(model, params, type, limit);
			}
			return model;
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e.getMessage(), e);
			}
			throw new WebScriptException("[ShareStats - SelectAudits] Error in executeImpl function");
		}
	}

	public void checkForQuery(Map<String, Object> model, AuditQueryParameters params, String type)
			throws SQLException, JSONException {
		checkForQuery(model, params, type, 0);
	}

	public void checkForQuery(Map<String, Object> model, AuditQueryParameters params, String type, int limit)
			throws SQLException, JSONException {
		switch (queryType.valueOf(type)) {
		case read:
			model.put("dates", selectByDate(params, "alfresco.atolcd.audit.selectByRead"));
			break;
		case created:
			model.put("dates", selectByDate(params, "alfresco.atolcd.audit.selectByCreated"));
			break;
		case deleted:
			model.put("dates", selectByDate(params, "alfresco.atolcd.audit.selectByDeleted"));
			break;
		case updated:
			model.put("dates", selectByDate(params, "alfresco.atolcd.audit.selectByUpdated"));
			break;
		case mostread:
			model.put("popularity", selectByPopularity(params, "alfresco.atolcd.audit.selectByMostRead", limit));
			break;
		case usermostread:
			model.put("popularity", selectByPopularity(params, "alfresco.atolcd.audit.selectByUserMostRead", limit));
			break;
		case mostupdated:
			model.put("popularity", selectByPopularity(params, "alfresco.atolcd.audit.selectByMostUpdated", limit));
		}
	}

	public List<AuditObjectPopularity> selectByPopularity(AuditQueryParameters params, String query, int limit) {
		List<AuditObjectPopularity> auditObjectPopularityList = new ArrayList();
		auditObjectPopularityList = this.sqlSessionTemplate.selectList(query, params);
		logger.info("Performing " + query + " ... ");

		Iterator<AuditObjectPopularity> iterator = auditObjectPopularityList.iterator();
		int treatedItems = 0;
		while ((iterator.hasNext()) && (treatedItems < limit)) {
			AuditObjectPopularity auditObjectPopularity = (AuditObjectPopularity) iterator.next();
			try {
				NodeRef nodeRef = new NodeRef(auditObjectPopularity.getAuditObject());
				if (!this.nodeService.exists(nodeRef)) {
					iterator.remove();
				} else {
					logger.debug("hasPermission?? "
							+ this.permissionService.hasPermission(nodeRef, "Read").equals(AccessStatus.ALLOWED));
					if (this.permissionService.hasPermission(nodeRef, "Read").equals(AccessStatus.ALLOWED)) {
						auditObjectPopularity
								.setObjectName((String) this.nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
						auditObjectPopularity.setObjectDisplayName(getPrettyDisplayname(nodeRef));

						Date creationDate = (Date) this.nodeService.getProperty(nodeRef, ContentModel.PROP_CREATED);
						float days = daysBetween(creationDate, new Date());
						float average = auditObjectPopularity.getPopularity() / days;
						BigDecimal roundfinalAverage = new BigDecimal(average).setScale(1, 4);
						auditObjectPopularity.setAverage(roundfinalAverage.floatValue());

						SiteInfo si = this.siteService.getSite(nodeRef);
						if (si != null) {
							Path nodePath = this.nodeService.getPath(nodeRef);
							if (nodePath.size() > 4) {
								String siteContainerQName = this.nodeService.getPath(nodeRef).get(4).getElementString();
								auditObjectPopularity
										.setSiteComponent(QName.createQName(siteContainerQName).getLocalName());
							}
						}
						treatedItems++;
					} else {
						iterator.remove();
					}
				}
			} catch (AlfrescoRuntimeException e) {
				iterator.remove();
				logger.warn(e.getMessage(), e);
			}
		}
		Collections.sort(auditObjectPopularityList, new AverageComparator());
		limit = auditObjectPopularityList.size() > limit ? limit : auditObjectPopularityList.size();
		return auditObjectPopularityList.subList(0, limit);
	}

	public List<List<AuditCount>> selectByDate(AuditQueryParameters params, String query) {
		String[] dates = params.getSlicedDates().split(",");
		List<List<AuditCount>> auditCount = new ArrayList();
		for (int i = 0; i < dates.length - 1; i++) {
			params.setDateFrom(dates[i]);
			params.setDateTo(dates[(i + 1)]);
			List<AuditCount> auditSample = new ArrayList();
			auditSample = this.sqlSessionTemplate.selectList(query, params);
			auditCount.add(auditSample);
		}
		logger.info("Performing " + query + " ... ");
		return auditCount;
	}

	public List<AuditEntry> selectEntriesToUpdate() {
		return this.sqlSessionTemplate.selectList("alfresco.atolcd.audit.selectEntriesToUpdate");
	}

	public AuditQueryParameters buildParametersFromRequest(WebScriptRequest req) {
		try {
			String dateFrom = req.getParameter("from");
			String dateTo = req.getParameter("to");

			AuditQueryParameters params = new AuditQueryParameters();
			params.setSiteId(req.getParameter("site"));
			params.setSitesId(req.getParameter("sites"));
			params.setActionName(req.getParameter("action"));
			params.setAppName(req.getParameter("module"));
			params.setAppNames(req.getParameter("modules"));
			params.setDateFrom(dateFrom);
			params.setDateTo(dateTo);
			params.setSlicedDates(req.getParameter("dates"));
			params.setUserId(req.getParameter("user"));
			params.setUsersId(req.getParameter("users"));
			return params;
		} catch (Exception e) {
			logger.error("Error building parameters", e);
		}
		return null;
	}

	private float daysBetween(Date d1, Date d2) {
		logger.debug("d1: " + d1);
		logger.debug("d2: " + d2);

		float days = (float) ((d2.getTime() - d1.getTime()) / 86400000L);
		if (days < 1.0F) {
			days = 1.0F;
		}
		logger.debug("days between: " + days);

		return days;
	}

	private String getPrettyDisplayname(NodeRef nodeRef) {
		String nodeName = (String) this.nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

		QName nodeType = this.nodeService.getType(nodeRef);
		if (nodeType.equals(TYPE_DATALIST)) {
			return (String) this.nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
		}
		if (nodeType.equals(ForumModel.TYPE_TOPIC)) {
			NodeRef firstTopic = this.nodeService.getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS, nodeName);
			if (firstTopic != null) {
				return (String) this.nodeService.getProperty(firstTopic, ContentModel.PROP_TITLE);
			}
		} else {
			if (nodeType.equals(TYPE_LINK)) {
				return (String) this.nodeService.getProperty(nodeRef, PROP_LINK_TITLE);
			}
			if (nodeType.equals(TYPE_CALENDAR_EVENT)) {
				return (String) this.nodeService.getProperty(nodeRef, PROP_CALENDAR_EVENT_WHAT);
			}
			NodeRef parentRef = this.nodeService.getPrimaryParent(nodeRef).getParentRef();
			if ((parentRef != null) && (this.nodeService.hasAspect(parentRef, SiteModel.ASPECT_SITE_CONTAINER))) {
				String parentName = (String) this.nodeService.getProperty(parentRef, ContentModel.PROP_NAME);
				if ((parentName.equals("blog")) || (parentName.equals("wiki"))) {
					return (String) this.nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
				}
			}
		}
		return nodeName;
	}
}
