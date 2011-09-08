package com.atolcd.apca.web.scripts.shareStats;

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

import com.atolcd.apca.ApcaAuditCount;
import com.atolcd.apca.ApcaAuditEntry;
import com.atolcd.apca.ApcaAuditQueryParameters;

public class SelectAuditsGet extends DeclarativeWebScript implements
		InitializingBean {
	// SqlMapClientTemplate for ibatis calls
	private SqlMapClientTemplate sqlMapClientTemplate;

	private static final String SELECT_ALL = "alfresco.apca.audit.selectAll";
	private static final String SELECT_ACTION = "alfresco.apca.audit.selectActions";
	private static final String SELECT_COMMENT = "alfresco.apca.audit.selectComments";
	private static final String SELECT_FILE = "alfresco.apca.audit.selectFileActions";
	private static final String SELECT_MODULE = "alfresco.apca.audit.selectModules";
	private static final String SELECT_MODULE_VIEW = "alfresco.apca.audit.selectModuleViews";
	// Requête entre sites
	private static final String SELECT_SITES_VIEW = "alfresco.apca.audit.selectViewsBySite";
	private static final String SELECT_SITES_FILE = "alfresco.apca.audit.selectFilesBySite";
	private static final String SELECT_SITES_COMMENT = "alfresco.apca.audit.selectCommentsBySite";
	// logger
	private static final Log logger = LogFactory.getLog(SelectAuditsGet.class);

	public void setSqlMapClientTemplate(
			SqlMapClientTemplate sqlMapClientTemplate) {
		this.sqlMapClientTemplate = sqlMapClientTemplate;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.sqlMapClientTemplate);
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req,
			Status status, Cache cache) {
		try {
			// Map passé au template.
			Map<String, Object> model = new HashMap<String, Object>();

			// Check for the sqlMapClientTemplate Bean
			if (this.sqlMapClientTemplate != null) {
				// Get the input content given into the request.
				// String jsonArg = req.getContent().getContent();

				String type = req.getParameter("type");
				// Inutile ?
				if (type.equals("all")) {
					model.put("results", selectAll());
				} else if (type.equals("module")) {
					ApcaAuditQueryParameters params = buildParametersFromRequest(req);
					model.put("views", select(params, SELECT_MODULE));
				} else if (type.equals("action")) {
					ApcaAuditQueryParameters params = buildParametersFromRequest(req);
					model.put("views", select(params, SELECT_ACTION));
				} else if (type.equals("module-views")) {
					ApcaAuditQueryParameters params = buildParametersFromRequest(req);
					model.put("views", select(params, SELECT_MODULE_VIEW));
				} else if (type.equals("file")) {
					ApcaAuditQueryParameters params = buildParametersFromRequest(req);
					model.put("views", select(params, SELECT_FILE));
				} else if (type.equals("comment")) {
					ApcaAuditQueryParameters params = buildParametersFromRequest(req);
					model.put("views", select(params, SELECT_COMMENT));
				} else if (type.equals("sites_view")) {
					ApcaAuditQueryParameters params = buildParametersFromRequest(req);
					model.put("views", select(params, SELECT_SITES_VIEW));
				} else if (type.equals("sites_comment")) {
					ApcaAuditQueryParameters params = buildParametersFromRequest(req);
					model.put("views", select(params, SELECT_SITES_COMMENT));
				} else if (type.equals("sites_file")) {
					ApcaAuditQueryParameters params = buildParametersFromRequest(req);
					model.put("views", select(params, SELECT_SITES_FILE));
				} else if (type.equals("module-by-month")
						|| type.equals("module-by-week")
						|| type.equals("module-by-day")) {
					ApcaAuditQueryParameters params = buildParametersFromRequest(req);
					model.put("slicedDates", params.getSlicedDates());
					model.put("dates", selectByDate(params, SELECT_MODULE));
				} else if (type.equals("module-views-by-month")
						|| type.equals("module-views-by-week")
						|| type.equals("module-views-by-day")) {
					ApcaAuditQueryParameters params = buildParametersFromRequest(req);
					model.put("slicedDates", params.getSlicedDates());
					model.put("dates", selectByDate(params, SELECT_MODULE_VIEW));
				} else if (type.equals("action-by-month")
						|| type.equals("action-by-week")
						|| type.equals("action-by-day")) {
					ApcaAuditQueryParameters params = buildParametersFromRequest(req);
					model.put("slicedDates", params.getSlicedDates());
					model.put("dates", selectByDate(params, SELECT_ACTION));
				} else if (type.equals("file-by-month")
						|| type.equals("file-by-week")
						|| type.equals("file-by-day")) {
					ApcaAuditQueryParameters params = buildParametersFromRequest(req);
					model.put("slicedDates", params.getSlicedDates());
					model.put("dates", selectByDate(params, SELECT_FILE));
				} else if (type.equals("comment-by-month")
						|| type.equals("comment-by-week")
						|| type.equals("comment-by-day")) {
					ApcaAuditQueryParameters params = buildParametersFromRequest(req);
					model.put("slicedDates", params.getSlicedDates());
					model.put("dates", selectByDate(params, SELECT_COMMENT));
				} else if (type.equals("sites_view-by-month")
						|| type.equals("sites_view-by-week")
						|| type.equals("sites_view-by-day")) {
					ApcaAuditQueryParameters params = buildParametersFromRequest(req);
					model.put("slicedDates", params.getSlicedDates());
					model.put("dates", selectByDate(params, SELECT_SITES_VIEW));
				} else if (type.equals("sites_comment-by-month")
						|| type.equals("sites_comment-by-week")
						|| type.equals("sites_comment-by-day")) {
					ApcaAuditQueryParameters params = buildParametersFromRequest(req);
					model.put("slicedDates", params.getSlicedDates());
					model.put("dates",
							selectByDate(params, SELECT_SITES_COMMENT));
				} else if (type.equals("sites_file-by-month")
						|| type.equals("sites_file-by-week")
						|| type.equals("sites_file-by-day")) {
					ApcaAuditQueryParameters params = buildParametersFromRequest(req);
					model.put("slicedDates", params.getSlicedDates());
					model.put("dates", selectByDate(params, SELECT_SITES_FILE));
				}
				model.put("type", type);
			}
			return model;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebScriptException(
					"[Apca-DbSelect] Error in executeImpl function");
		}
	}

	/**
	 *
	 * @return
	 * @throws SQLException
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	public List<ApcaAuditEntry> selectAll() throws SQLException, JSONException {
		List<ApcaAuditEntry> auditSamples = new ArrayList<ApcaAuditEntry>();
		auditSamples = sqlMapClientTemplate.queryForList(SELECT_ALL);
		logger.info("Performing selectAll() ... ");
		return auditSamples;
	}

	@SuppressWarnings("unchecked")
	public List<ApcaAuditCount> select(ApcaAuditQueryParameters params,
			String query) {
		List<ApcaAuditCount> auditCount = new ArrayList<ApcaAuditCount>();
		auditCount = sqlMapClientTemplate.queryForList(query, params);
		logger.info("Performing " + query + " ... ");
		return auditCount;
	}

	@SuppressWarnings("unchecked")
	public List<List<ApcaAuditCount>> selectByDate(
			ApcaAuditQueryParameters params, String query) {
		String[] dates = params.getSlicedDates().split(",");
		List<List<ApcaAuditCount>> auditCount = new ArrayList<List<ApcaAuditCount>>();
		for (int i = 0; i < dates.length - 1; i++) {
			params.setDateFrom(dates[i]);
			params.setDateTo(dates[i + 1]);
			List<ApcaAuditCount> auditSample = new ArrayList<ApcaAuditCount>();
			auditSample = sqlMapClientTemplate.queryForList(query, params);
			auditCount.add(auditSample);
		}
		logger.info("Performing " + query + " ... ");
		return auditCount;
	}

	public ApcaAuditQueryParameters buildParametersFromRequest(
			WebScriptRequest req) {
		try {
			// Users and object to define ...

			// Problème de long / null
			String dateFrom = req.getParameter("from");
			String dateTo = req.getParameter("to");

			ApcaAuditQueryParameters params = new ApcaAuditQueryParameters();
			params.setSiteId(req.getParameter("site"));
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
