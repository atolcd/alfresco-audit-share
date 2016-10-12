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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.Assert;

import com.atolcd.alfresco.AuditQueryParameters;

public class DeleteAuditDelete extends DeclarativeWebScript implements InitializingBean {
	// Logger
	private static final Log logger = LogFactory.getLog(DeleteAuditDelete.class);

	// SqlMapClientTemplate for MyBatis calls
	private static SqlSessionTemplate sqlSessionTemplate;

	// MyBatis query ids
	private static final String DELETE_AUDIT_BY_PARAMETERS = "alfresco.atolcd.audit.deleteAuditByParameters";
	private static final String DELETE_VOLUMETRY_BY_PARAMETERS = "alfresco.atolcd.audit.deleteVolumetryByParameters";
	private static final String MODEL_SUCCESS = "success";

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		DeleteAuditDelete.sqlSessionTemplate = sqlSessionTemplate;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(sqlSessionTemplate);
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_SUCCESS, false);
		try {
			// Check for the sqlMapClientTemplate Bean
			if (this.sqlSessionTemplate != null) {
				String purgeTable = req.getParameter("purgeTable");
				AuditQueryParameters auditQueryParameters = buildParametersFromRequest(req);

				if (auditQueryParameters != null) {
					if ("audit_entry".equals(purgeTable)) {
						deleteAuditEntries(auditQueryParameters.getDateFrom(), auditQueryParameters.getDateTo(),
						    auditQueryParameters.getSiteId());
						model.put(MODEL_SUCCESS, true);

					} else if ("volumetry".equals(purgeTable)) {
						deleteVolumetryEntries(auditQueryParameters.getDateFrom(), auditQueryParameters.getDateTo(),
						    auditQueryParameters.getSiteId());
						model.put(MODEL_SUCCESS, true);

					} else if ("all".equals(purgeTable) || purgeTable == null) {
						deleteAuditEntries(auditQueryParameters.getDateFrom(), auditQueryParameters.getDateTo(),
						    auditQueryParameters.getSiteId());
						deleteVolumetryEntries(auditQueryParameters.getDateFrom(), auditQueryParameters.getDateTo(),
						    auditQueryParameters.getSiteId());
						model.put(MODEL_SUCCESS, true);
					}
				}
			}
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e.getMessage(), e);
			}
			throw new WebScriptException("[ShareStats-DbDelete] Error in executeImpl function");
		}
		return model;
	}

	/**
	 * Supprime des entrées d'audit
	 *
	 * @param from
	 *          Date from (timestamp)
	 * @param to
	 *          Date to (timestamp)
	 * @param site
	 *          Site
	 */
	public static void deleteAuditEntries(long from, long to, String site) {
		AuditQueryParameters auditQueryParameters = new AuditQueryParameters();
		auditQueryParameters.setDateFrom(from);
		auditQueryParameters.setDateTo(to);
		auditQueryParameters.setSiteId(site);
		sqlSessionTemplate.delete(DELETE_AUDIT_BY_PARAMETERS, auditQueryParameters);
		if (logger.isDebugEnabled()) {
			logger.debug("Audits successfully deleted.");
		}
	}

	// For delete volumetry entries
	public void deleteVolumetryEntries(long from, long to, String site) {
		AuditQueryParameters auditQueryParameters = new AuditQueryParameters();
		auditQueryParameters.setDateFrom(from);
		auditQueryParameters.setDateTo(to);
		auditQueryParameters.setSiteId(site);
		sqlSessionTemplate.delete(DELETE_VOLUMETRY_BY_PARAMETERS, auditQueryParameters);
		if (logger.isDebugEnabled()) {
			logger.debug("Volumetry successfully deleted.");
		}
	}

	// Recovery the webscript parameters
	public AuditQueryParameters buildParametersFromRequest(WebScriptRequest req) {
		try {
			String dateFrom = req.getParameter("from");
			String dateTo = req.getParameter("to");

			AuditQueryParameters params = new AuditQueryParameters();
			params.setSiteId(req.getParameter("site"));
			params.setSitesId(req.getParameter("sites"));
			params.setDateFrom(dateFrom);
			params.setDateTo(dateTo);
			return params;
		} catch (Exception e) {
			logger.error("Error building parameters", e);
			return null;
		}
	}
}