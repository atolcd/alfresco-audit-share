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
	// SqlMapClientTemplate for ibatis calls
	private static SqlSessionTemplate sqlSessionTemplate;

	// Identifiant de la requête côté iBatis
	private static final String DELETE_BY_PARAMETERS = "alfresco.atolcd.audit.deleteByParameters";
	private static final String MSG_OK = "La suppression des audits s'est déroulée correctement.";
	private static final String MODEL_SUCCESS = "success";

	// logger
	private static final Log logger = LogFactory.getLog(DeleteAuditDelete.class);

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

		// TODO : A implémenter avec une interface administration

		model.put(MODEL_SUCCESS, true);
		return model;
	}

	/**
	 * Supprime des entrées d'audit
	 * 
	 * @param auditEntries
	 */
	public static void deleteAuditEntries(long from, long to) {
		AuditQueryParameters auditQueryParameters = new AuditQueryParameters();
		auditQueryParameters.setDateFrom(from);
		auditQueryParameters.setDateTo(to);
		sqlSessionTemplate.delete(DELETE_BY_PARAMETERS, auditQueryParameters);
		if (logger.isDebugEnabled()) {
			logger.debug(MSG_OK);
		}
	}
}
