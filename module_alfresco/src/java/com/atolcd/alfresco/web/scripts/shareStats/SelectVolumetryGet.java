package com.atolcd.alfresco.web.scripts.shareStats;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.orm.ibatis.SqlMapClientTemplate;
import org.springframework.util.Assert;

import com.atolcd.alfresco.AuditQueryParameters;

public class SelectVolumetryGet extends DeclarativeWebScript implements InitializingBean {
	private SqlMapClientTemplate sqlMapClientTemplate;
	// logger
	private static final Log logger = LogFactory.getLog(SelectVolumetryGet.class);

	private static final String SELECT_VOLUMETRY = "alfresco.atolcd.audit.selectVolumetry";

	public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate) {
		this.sqlMapClientTemplate = sqlMapClientTemplate;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		Assert.notNull(sqlMapClientTemplate);
	}

	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		try {
			Map<String, Object> model = new HashMap<String, Object>();

			if (this.sqlMapClientTemplate != null) {
				AuditQueryParameters params = buildParametersFromRequest(req);

				String[] dates = params.getSlicedDates().split(",");
				long[] values = new long[dates.length - 1];
				for (int i = 0; i < dates.length - 1; i++) {
					params.setDateFrom(dates[i]);
					params.setDateTo(dates[i + 1]);
					Object o = sqlMapClientTemplate.queryForObject(SELECT_VOLUMETRY, params);
					if(o == null){
						values[i] = 0;
					} else {
						values[i] = (Long)o;
					}
				}
				model.put("values", values);
			}
			return model;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebScriptException("[ShareStats - SelectVolumetry] Error in executeImpl function");
		}
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
}
