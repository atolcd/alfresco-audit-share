package com.atolcd.alfresco.web.scripts.shareStats;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.atolcd.alfresco.AuditCount;
import com.atolcd.alfresco.AuditEntry;
import com.atolcd.alfresco.AuditQueryParameters;

public class SelectAuditsGet extends DeclarativeWebScript implements InitializingBean {
	// SqlMapClientTemplate for ibatis calls
	private SqlMapClientTemplate sqlMapClientTemplate;

	private static final String SELECT_ALL = "alfresco.atolcd.audit.selectAll";
	private static final String SELECT_ACTION = "alfresco.atolcd.audit.selectActions";
	private static final String SELECT_COMMENT = "alfresco.atolcd.audit.selectComments";
	private static final String SELECT_FILE = "alfresco.atolcd.audit.selectFileActions";
	private static final String SELECT_MODULE = "alfresco.atolcd.audit.selectModules";
	private static final String SELECT_MODULE_VIEW = "alfresco.atolcd.audit.selectModuleViews";
	// Requête entre sites
	private static final String SELECT_SITES_VIEW = "alfresco.atolcd.audit.selectViewsBySite";
	private static final String SELECT_SITES_FILE = "alfresco.atolcd.audit.selectFilesBySite";
	private static final String SELECT_SITES_COMMENT = "alfresco.atolcd.audit.selectCommentsBySite";
	// logger
	private static final Log logger = LogFactory.getLog(SelectAuditsGet.class);

	public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate) {
		this.sqlMapClientTemplate = sqlMapClientTemplate;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.sqlMapClientTemplate);
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		try {
			// Map passé au template.
			Map<String, Object> model = new HashMap<String, Object>();

			// Check for the sqlMapClientTemplate Bean
			if (this.sqlMapClientTemplate != null) {
				// Get the input content given into the request.
				// String jsonArg = req.getContent().getContent();
				AuditQueryParameters params = buildParametersFromRequest(req);
				String type = req.getParameter("type");
				checkForQuery(model, params, type);
			}
			return model;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebScriptException("[ShareStats - SelectAudits] Error in executeImpl function");
		}
	}

	public void checkForQuery(Map<String, Object> model, AuditQueryParameters params, String type) throws SQLException, JSONException {
		switch (queryType.valueOf(type)) {
		case all:
			model.put("results", selectAll(params));
			break;
		case module:
			model.put("views", select(params, SELECT_MODULE));
			break;
		case action:
			model.put("views", select(params, SELECT_ACTION));
			break;
		case file:
			model.put("views", select(params, SELECT_FILE));
			break;
		case comment:
			model.put("views", select(params, SELECT_COMMENT));
			break;
		case moduleviews:
			model.put("views", select(params, SELECT_MODULE_VIEW));
			break;
		case sitesview:
			model.put("views", select(params, SELECT_SITES_VIEW));
			break;
		case sitescomment:
			model.put("views", select(params, SELECT_SITES_COMMENT));
			break;
		case sitesfile:
			model.put("views", select(params, SELECT_SITES_FILE));
			break;
		case module_by_month:
			model.put("dates", selectByDate(params, SELECT_MODULE));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case module_by_week:
			model.put("dates", selectByDate(params, SELECT_MODULE));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case module_by_day:
			model.put("dates", selectByDate(params, SELECT_MODULE));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case moduleviews_by_month:
			model.put("dates", selectByDate(params, SELECT_MODULE_VIEW));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case moduleviews_by_week:
			model.put("dates", selectByDate(params, SELECT_MODULE_VIEW));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case moduleviews_by_day:
			model.put("dates", selectByDate(params, SELECT_MODULE_VIEW));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case action_by_month:
			model.put("dates", selectByDate(params, SELECT_ACTION));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case action_by_week:
			model.put("dates", selectByDate(params, SELECT_ACTION));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case action_by_day:
			model.put("dates", selectByDate(params, SELECT_ACTION));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case file_by_month:
			model.put("dates", selectByDate(params, SELECT_FILE));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case file_by_week:
			model.put("dates", selectByDate(params, SELECT_FILE));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case file_by_day:
			model.put("dates", selectByDate(params, SELECT_FILE));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case comment_by_month:
			model.put("dates", selectByDate(params, SELECT_COMMENT));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case comment_by_week:
			model.put("dates", selectByDate(params, SELECT_COMMENT));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case comment_by_day:
			model.put("dates", selectByDate(params, SELECT_COMMENT));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case sitesview_by_month:
			model.put("dates", selectByDate(params, SELECT_SITES_VIEW));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case sitesview_by_week:
			model.put("dates", selectByDate(params, SELECT_SITES_VIEW));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case sitesview_by_day:
			model.put("dates", selectByDate(params, SELECT_SITES_VIEW));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case sitescomment_by_month:
			model.put("dates", selectByDate(params, SELECT_SITES_COMMENT));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case sitescomment_by_week:
			model.put("dates", selectByDate(params, SELECT_SITES_COMMENT));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case sitescomment_by_day:
			model.put("dates", selectByDate(params, SELECT_SITES_COMMENT));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case sitesfile_by_month:
			model.put("dates", selectByDate(params, SELECT_SITES_FILE));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case sitesfile_by_week:
			model.put("dates", selectByDate(params, SELECT_SITES_FILE));
			model.put("slicedDates", params.getSlicedDates());
			break;
		case sitesfile_by_day:
			model.put("dates", selectByDate(params, SELECT_SITES_FILE));
			model.put("slicedDates", params.getSlicedDates());
			break;
		}
		model.put("type", type);
	}

	/**
	 * 
	 * @return
	 * @throws SQLException
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	public List<AuditEntry> selectAll(AuditQueryParameters params) throws SQLException, JSONException {
		List<AuditEntry> auditSamples = new ArrayList<AuditEntry>();
		auditSamples = sqlMapClientTemplate.queryForList(SELECT_ALL, params);
		logger.info("Performing selectAll() ... ");
		return auditSamples;
	}

	@SuppressWarnings("unchecked")
	public List<AuditCount> select(AuditQueryParameters params, String query) {
		List<AuditCount> auditCount = new ArrayList<AuditCount>();
		auditCount = sqlMapClientTemplate.queryForList(query, params);
		logger.info("Performing " + query + " ... ");
		return auditCount;
	}

	@SuppressWarnings("unchecked")
	public List<List<AuditCount>> selectByDate(AuditQueryParameters params, String query) {
		String[] dates = params.getSlicedDates().split(",");
		List<List<AuditCount>> auditCount = new ArrayList<List<AuditCount>>();
		for (int i = 0; i < dates.length - 1; i++) {
			params.setDateFrom(dates[i]);
			params.setDateTo(dates[i + 1]);
			List<AuditCount> auditSample = new ArrayList<AuditCount>();
			auditSample = sqlMapClientTemplate.queryForList(query, params);
			auditCount.add(auditSample);
		}
		logger.info("Performing " + query + " ... ");
		return auditCount;
	}

	public AuditQueryParameters buildParametersFromRequest(WebScriptRequest req) {
		try {
			// Users and object to define ...

			// Problème de long / null
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
			logger.error("Erreur lors de la construction des paramètres [select.java]");
			e.printStackTrace();
			return null;
		}
	}

}
