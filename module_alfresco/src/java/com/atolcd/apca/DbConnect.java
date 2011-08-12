package com.atolcd.apca;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.orm.ibatis.SqlMapClientTemplate;
import org.springframework.util.Assert;

public class DbConnect extends DeclarativeWebScript implements InitializingBean {
	//SqlMapClientTemplate for ibatis calls
	private SqlMapClientTemplate sqlMapClientTemplate;
	
	
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
			
			//Fill an auditSample from the request content and insert it
			ApcaAuditEntry auditSample = new ApcaAuditEntry(jsonArg);
			insert(auditSample);
			//update(1);
		}
		return model;
		} catch (Exception e){
			throw new WebScriptException("[Apca-DbConnect] Error in executeImpl function");
		}
	}
	
	/**
	 * @useless
	 * 
	 * @param l long Id de l'enregistrement à modifier
	 * @throws SQLException
	 * @throws JSONException
	 */
	public void update(long l) throws SQLException, JSONException{
		ApcaAuditEntry auditSample = this.select(l);
		auditSample.setAuditAppName("Forum");
		sqlMapClientTemplate.update("alfresco.apca.audit.updateById", auditSample);
		System.out.println("Update by JSON ok : " + auditSample.toJSON());		
	}
	
	public void insert(ApcaAuditEntry auditSample)throws SQLException, JSONException{
		//TODO Vérification des valeurs qui vont être ajoutée
		sqlMapClientTemplate.insert("alfresco.apca.audit.insertEntry", auditSample);
		System.out.println("Insert by JSON ok : " + auditSample.toJSON());
	}
	
	public ApcaAuditEntry select(long l) throws SQLException, JSONException{
		ApcaAuditEntry auditSample = (ApcaAuditEntry) sqlMapClientTemplate.queryForObject("alfresco.apca.audit.selectById",l);
		System.out.println("Select by JSON ok : " + auditSample.toJSON());	
		return auditSample;
	}
}
