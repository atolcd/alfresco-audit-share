package com.atolcd.apca.web.scripts.shareStats;

import java.sql.SQLException;
import java.util.HashMap;
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

import com.atolcd.apca.ApcaAuditEntry;

public class InsertAuditPost extends DeclarativeWebScript implements InitializingBean {
	private static final String INSERT_ENTRY = "alfresco.apca.audit.insertEntry";
	//SqlMapClientTemplate for ibatis calls
	private SqlMapClientTemplate sqlMapClientTemplate;

	private static final Log logger = LogFactory.getLog(InsertAuditPost.class);

	public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate){
		this.sqlMapClientTemplate = sqlMapClientTemplate;
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.sqlMapClientTemplate);
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache){
		try{
		// Map that will be passed to the template
		Map<String, Object> model = new HashMap<String, Object>();

		// Check for the sqlMapClientTemplate Bean
		if(this.sqlMapClientTemplate != null){
			//Get the input content given into the request.
			String jsonArg = req.getContent().getContent();

			if(!jsonArg.isEmpty()){
				//Fill an auditSample from the request content and insert it
				ApcaAuditEntry auditSample = new ApcaAuditEntry(jsonArg);
				insert(auditSample);
				model.put("success", true);
			}
			else{
				model.put("success", false);
			}
		}
		return model;
		} catch (Exception e){
			throw new WebScriptException("[Apca-DbInsert] Error in executeImpl function");
		}
	}


	public void insert(ApcaAuditEntry auditSample)throws SQLException, JSONException{
		sqlMapClientTemplate.insert(INSERT_ENTRY, auditSample);
		logger.info("Insert ok : " + auditSample.toJSON());

	}
}
