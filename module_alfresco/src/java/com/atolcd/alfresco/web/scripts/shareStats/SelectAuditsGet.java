package com.atolcd.alfresco.web.scripts.shareStats;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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
import com.atolcd.alfresco.AuditObjectPopularity;
import com.atolcd.alfresco.AuditQueryParameters;

public class SelectAuditsGet extends DeclarativeWebScript implements InitializingBean {
	// SqlMapClientTemplate for ibatis calls
	private SqlSessionTemplate sqlSessionTemplate;
	private NodeService nodeService;

	private static final String SELECT_BY_VIEW = "alfresco.atolcd.audit.selectByRead";
	private static final String SELECT_BY_CREATED = "alfresco.atolcd.audit.selectByCreated";
	private static final String SELECT_BY_UPDATED = "alfresco.atolcd.audit.selectByUpdated";
	private static final String SELECT_BY_DELETED = "alfresco.atolcd.audit.selectByDeleted";
	private static final String SELECT_BY_MOSTREAD = "alfresco.atolcd.audit.selectByMostRead";
	private static final String SELECT_BY_MOSTUPDATED = "alfresco.atolcd.audit.selectByMostUpdated";

	static final QName TYPE_DATALIST = QName.createQName("http://www.alfresco.org/model/datalist/1.0", "dataList");
	static final QName TYPE_CALENDAR_EVENT = QName.createQName("http://www.alfresco.org/model/calendar", "calendarEvent");
	static final QName PROP_CALENDAR_EVENT_WHAT = QName.createQName("http://www.alfresco.org/model/calendar", "whatEvent");
	static final QName TYPE_LINK = QName.createQName("http://www.alfresco.org/model/linksmodel/1.0", "link");
	static final QName PROP_LINK_TITLE = QName.createQName("http://www.alfresco.org/model/linksmodel/1.0", "title");

	// logger
	private static final Log logger = LogFactory.getLog(SelectAuditsGet.class);

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.sqlSessionTemplate);
		Assert.notNull(this.nodeService);
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		try {
			// Map passÃ© au template.
			Map<String, Object> model = new HashMap<String, Object>();

			// Check for the sqlMapClientTemplate Bean
			if (this.sqlSessionTemplate != null) {
				// Get the input content given into the request.
				// String jsonArg = req.getContent().getContent();
				AuditQueryParameters params = buildParametersFromRequest(req);
				String type = req.getParameter("type");
				String stringLimit = req.getParameter("limit");
				int limit = 0;
				if (stringLimit != null && !stringLimit.isEmpty()) {
					limit = Integer.parseInt(stringLimit);
				}
				checkForQuery(model, params, type, limit);
			}
			return model;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebScriptException("[ShareStats - SelectAudits] Error in executeImpl function");
		}
	}

	public void checkForQuery(Map<String, Object> model, AuditQueryParameters params, String type) throws SQLException, JSONException {
		checkForQuery(model, params, type, 0);
	}

	public void checkForQuery(Map<String, Object> model, AuditQueryParameters params, String type, int limit) throws SQLException,
			JSONException {
		switch (queryType.valueOf(type)) {
		case read:
			model.put("dates", selectByDate(params, SELECT_BY_VIEW));
			break;
		case created:
			model.put("dates", selectByDate(params, SELECT_BY_CREATED));
			break;
		case deleted:
			model.put("dates", selectByDate(params, SELECT_BY_DELETED));
			break;
		case updated:
			model.put("dates", selectByDate(params, SELECT_BY_UPDATED));
			break;
		case mostread:
			model.put("popularity", selectByPopularity(params, SELECT_BY_MOSTREAD, limit));
			break;
		case mostupdated:
			model.put("popularity", selectByPopularity(params, SELECT_BY_MOSTUPDATED, limit));
			break;
		}
	}

	@SuppressWarnings("unchecked")
	public List<AuditObjectPopularity> selectByPopularity(AuditQueryParameters params, String query, int limit) {
		List<AuditObjectPopularity> auditObjectPopularityList = new ArrayList<AuditObjectPopularity>();
		auditObjectPopularityList = (List<AuditObjectPopularity>) sqlSessionTemplate.selectList(query, params);
		logger.info("Performing " + query + " ... ");

		Iterator<AuditObjectPopularity> iterator = auditObjectPopularityList.iterator();
		int treatedItems = 0;
		// On test si les éléments retournés existent toujours
		while (iterator.hasNext() && treatedItems < limit) {
			AuditObjectPopularity auditObjectPopularity = iterator.next();
			try {
				NodeRef nodeRef = new NodeRef(auditObjectPopularity.getAuditObject());
				if (!nodeService.exists(nodeRef)) {
					iterator.remove();
				} else {
					auditObjectPopularity.setObjectName((String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
					auditObjectPopularity.setObjectDisplayName(getPrettyDisplayname(nodeRef));
					treatedItems++;
				}
			} catch (AlfrescoRuntimeException e) {
				iterator.remove();
				logger.error(e);
			}
		}
		limit = auditObjectPopularityList.size() > limit ? limit : auditObjectPopularityList.size();
		return auditObjectPopularityList.subList(0, limit);
	}

	@SuppressWarnings("unchecked")
	public List<List<AuditCount>> selectByDate(AuditQueryParameters params, String query) {
		String[] dates = params.getSlicedDates().split(",");
		List<List<AuditCount>> auditCount = new ArrayList<List<AuditCount>>();
		for (int i = 0; i < dates.length - 1; i++) {
			params.setDateFrom(dates[i]);
			params.setDateTo(dates[i + 1]);
			List<AuditCount> auditSample = new ArrayList<AuditCount>();
			auditSample = (List<AuditCount>) sqlSessionTemplate.selectList(query, params);
			auditCount.add(auditSample);
		}
		logger.info("Performing " + query + " ... ");
		return auditCount;
	}

	public AuditQueryParameters buildParametersFromRequest(WebScriptRequest req) {
		try {
			// Probleme de long / null
			String dateFrom = req.getParameter("from");
			String dateTo = req.getParameter("to");

			AuditQueryParameters params = new AuditQueryParameters();
			params.setSiteId(req.getParameter("site"));
			params.setSitesId(req.getParameter("sites"));
			params.setActionName(req.getParameter("action"));
			params.setAppName(req.getParameter("module"));
			params.setDateFrom(dateFrom);
			params.setDateTo(dateTo);
			params.setSlicedDates(req.getParameter("dates"));
			return params;
		} catch (Exception e) {
			logger.error("Erreur lors de la construction des parametres [select.java]");
			e.printStackTrace();
			return null;
		}
	}

	private String getPrettyDisplayname(NodeRef nodeRef) {
		String nodeName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

		QName nodeType = nodeService.getType(nodeRef);
		if (nodeType.equals(TYPE_DATALIST)) {
			// DataList : titre
			return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
		} else if (nodeType.equals(ForumModel.TYPE_TOPIC)) {
			// Discussion : on récupère le titre de l'enfant du même nom qui est
			// la discussion principale
			NodeRef firstTopic = nodeService.getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS, nodeName);
			if (firstTopic != null) {
				return (String) nodeService.getProperty(firstTopic, ContentModel.PROP_TITLE);
			}
		} else if (nodeType.equals(TYPE_LINK)) {
			// Lien : titre du lien (modèle particulier)
			return (String) nodeService.getProperty(nodeRef, PROP_LINK_TITLE);
		} else if (nodeType.equals(TYPE_CALENDAR_EVENT)) {
			// Evenement
			return (String) nodeService.getProperty(nodeRef, PROP_CALENDAR_EVENT_WHAT);
		} else {
			// Others : content, wiki, blog
			NodeRef parentRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
			if (parentRef != null) {
				if (nodeService.hasAspect(parentRef, SiteModel.ASPECT_SITE_CONTAINER)) {
					String parentName = (String) nodeService.getProperty(parentRef, ContentModel.PROP_NAME);
					if (parentName.equals("blog") || parentName.equals("wiki")) {
						// Blog ou Wiki
						return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
					}
				}
			}
		}

		return nodeName;
	}
}
