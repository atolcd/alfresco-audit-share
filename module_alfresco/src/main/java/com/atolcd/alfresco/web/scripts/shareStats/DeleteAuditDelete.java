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
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.Assert;

import com.atolcd.alfresco.AuditQueryParameters;

public class DeleteAuditDelete extends DeclarativeWebScript implements InitializingBean {
	// Logger
	private static final Log logger = LogFactory.getLog(DeleteAuditDelete.class);

	// SqlMapClientTemplate for MyBatis calls
	private static SqlSessionTemplate sqlSessionTemplate;

	// MyBatis query ids
	private static final String DELETE_BY_PARAMETERS = "alfresco.atolcd.audit.deleteByParameters";
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

		// TODO: To be implemented for an administration interface

		model.put(MODEL_SUCCESS, true);
		return model;
	}

	/**
	 * Supprime des entrées d'audit
	 * 
	 * @param from
	 *            Date from (timestamp)
	 * @param to
	 *            Date to (timestamp)
	 */
	public static void deleteAuditEntries(long from, long to) {
		AuditQueryParameters auditQueryParameters = new AuditQueryParameters();
		auditQueryParameters.setDateFrom(from);
		auditQueryParameters.setDateTo(to);
		sqlSessionTemplate.delete(DELETE_BY_PARAMETERS, auditQueryParameters);
		if (logger.isDebugEnabled()) {
			logger.debug("Audits successfully deleted.");
		}
	}
}