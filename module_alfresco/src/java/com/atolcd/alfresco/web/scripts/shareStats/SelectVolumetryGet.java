package com.atolcd.alfresco.web.scripts.shareStats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
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
	private SiteService siteService;
	// logger
	private static final Log logger = LogFactory.getLog(SelectVolumetryGet.class);

	private static final String SELECT_VOLUMETRY = "alfresco.atolcd.audit.selectVolumetry";

	public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate) {
		this.sqlMapClientTemplate = sqlMapClientTemplate;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(sqlMapClientTemplate);
		Assert.notNull(siteService);
	}

	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		try {
			Map<String, Object> model = new HashMap<String, Object>();

			if (this.sqlMapClientTemplate != null) {
				AuditQueryParameters params = buildParametersFromRequest(req);

				String[] dates = params.getSlicedDates().split(",");
				Map<String, List<Long>> stackedValues = new HashMap<String, List<Long>>(dates.length - 1);
				List<Long> countValues = new ArrayList<Long>(dates.length - 1);

				List<String> siteIds = new ArrayList<String>();
				if (params.getSiteId() == null && params.getSitesId() == null) {
					siteIds = getAllSites();
				} else if (params.getSiteId() != null) {
					siteIds.add(params.getSiteId());
				} else if (params.getSitesId() != null) {
					siteIds = params.getSitesId();
				}

				// On travaille site par site
				params.setSitesId(Collections.<String> emptyList());

				for (int i = 0; i < dates.length - 1; i++) {
					params.setDateFrom(dates[i]);
					params.setDateTo(dates[i + 1]);

					List<Long> values = new ArrayList<Long>(siteIds.size());
					Long total = (long) 0;
					for (String site : siteIds) {
						params.setSiteId(site);
						Object o = sqlMapClientTemplate.queryForObject(SELECT_VOLUMETRY, params);
						if (o == null) {
							values.add(Long.valueOf(0));
						} else {
							values.add((Long) o);
							total += (Long) o;
						}
					}

					countValues.add(total);
					stackedValues.put(String.valueOf(i > 9 ? i : "0" + i), values);
				}

				model.put("values", countValues);
				model.put("stackedValues", stackedValues.entrySet());
				model.put("sites", siteIds);
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

	private List<String> getAllSites() {
		List<SiteInfo> sites = siteService.listSites("", "");
		if (sites != null && !sites.isEmpty()) {
			List<String> res = new ArrayList<String>(sites.size());
			for (SiteInfo siteInfo : sites) {
				res.add(siteInfo.getShortName());
			}

			return res;
		}

		return Collections.emptyList();
	}
}
