package com.atolcd.apca.database;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.orm.ibatis.SqlMapClientTemplate;
import org.springframework.util.Assert;

import com.atolcd.apca.ApcaAuditEntry;

public class Update extends DeclarativeWebScript implements InitializingBean {
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
			
			if(jsonArg.length() >0 ){
			    //TODO Select the good entries.
				JSONObject obj = new JSONObject(jsonArg);
				update(obj.getString("site"));
			}
		}
		return model;
		} catch (Exception e){
			throw new WebScriptException("[Apca-DbSelect] Error in executeImpl function");
		}
	}
	
	
	/**
	 * 
	 * @param l long Id de l'enregistrement à modifier
	 * @throws SQLException
	 * @throws JSONException
	 */
	public void update(String site) throws SQLException, JSONException{
    
		ApcaAuditEntry auditSample = this.selectEntriesToUpdate();
		auditSample.setAuditSite(site);
		sqlMapClientTemplate.update("alfresco.apca.audit.updateBySite", auditSample);	
	}
  
  public ApcaAuditEntry selectEntriesToUpdate() throws SQLException{
    //TODO
    ApcaAuditEntry auditSample = new ApcaAuditEntry();
    
    return auditSample;
  }
}
