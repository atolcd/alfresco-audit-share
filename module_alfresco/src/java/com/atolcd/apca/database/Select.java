package com.atolcd.apca.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import com.atolcd.apca.ApcaAuditEntry;
import com.atolcd.apca.ApcaAuditCount;
import com.atolcd.apca.ApcaAuditQueryParameters;

public class Select extends DeclarativeWebScript implements InitializingBean {
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
			//String jsonArg = req.getContent().getContent();

			String type = req.getParameter("type");
			if(type.equals("all")){
				model.put("results", selectAll());
			}
			else if(type.equals("module")) {
				ApcaAuditQueryParameters params = buildParametersFromRequest(req);
				model.put("views", selectByModuleWithStats(params));
			}
			else if(type.equals("action")){
				ApcaAuditQueryParameters params = buildParametersFromRequest(req);
				model.put("views", selectByActionWithStats(params));
			}
			else if(type.equals("module-views")){
				ApcaAuditQueryParameters params = buildParametersFromRequest(req);
				model.put("views", selectByModulesViews(params));
			}
			else if(type.equals("action-views")){
				ApcaAuditQueryParameters params = buildParametersFromRequest(req);
				model.put("views", selectByActionsViews(params));
			}
			else if(type.equals("by-month")){
				ApcaAuditQueryParameters params = buildParametersFromRequest(req);
				model.put("dates", selectByMonth(params));
			}
			else if(type.equals("by-day")){
				ApcaAuditQueryParameters params = buildParametersFromRequest(req);
				model.put("dates", selectByDay(params));
			}

		}
		return model;
		} catch (Exception e){
			e.printStackTrace();
			throw new WebScriptException("[Apca-DbSelect] Error in executeImpl function");

		}
	}


	public ApcaAuditEntry select(long l) throws SQLException, JSONException
	{
		ApcaAuditEntry auditSample = (ApcaAuditEntry) sqlMapClientTemplate.queryForObject("alfresco.apca.audit.selectById",l);
		System.out.println("Select by JSON ok : " + auditSample.toJSON());
		return auditSample;
	}

	/**
	 *
	 * @return
	 * @throws SQLException
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	public List<ApcaAuditEntry> selectAll() throws SQLException, JSONException
	{
		List<ApcaAuditEntry> auditSamples = new ArrayList<ApcaAuditEntry>();
		auditSamples = sqlMapClientTemplate.queryForList("alfresco.apca.audit.selectAll");
		System.out.println("Performing selectAll() ... ");
		return auditSamples;
	}

	/**
	 *
	 * @return
	 * @throws SQLException
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	public List<ApcaAuditCount> selectByModuleWithStats(ApcaAuditQueryParameters params) throws SQLException, JSONException
	{
		List<ApcaAuditCount> auditCount = new ArrayList<ApcaAuditCount>();
		auditCount = sqlMapClientTemplate.queryForList("alfresco.apca.audit.selectByModuleWithStats",params);
		System.out.println("Performing selectByModuleWithStats() ... ");
		return auditCount;
	}

	/**
	 *
	 * @return
	 * @throws SQLException
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	public List<ApcaAuditCount> selectByActionWithStats(ApcaAuditQueryParameters params) throws SQLException, JSONException
	{
		List<ApcaAuditCount> auditCount = new ArrayList<ApcaAuditCount>();
		auditCount = sqlMapClientTemplate.queryForList("alfresco.apca.audit.selectByActionWithStats",params);
		System.out.println("Performing selectByActionWithStats() ... ");
		return auditCount;
	}

	@SuppressWarnings("unchecked")
	public List<ApcaAuditCount> selectByModulesViews(ApcaAuditQueryParameters params)
	{
		List<ApcaAuditCount> auditCount = new ArrayList<ApcaAuditCount>();
		auditCount = sqlMapClientTemplate.queryForList("alfresco.apca.audit.selectByModulesViews",params);
		System.out.println("Performing selectByModuleViews() ... ");
		return auditCount;
	}

	@SuppressWarnings("unchecked")
	public List<ApcaAuditCount> selectByActionsViews(ApcaAuditQueryParameters params)
	{
		List<ApcaAuditCount> auditCount = new ArrayList<ApcaAuditCount>();
		auditCount = sqlMapClientTemplate.queryForList("alfresco.apca.audit.selectByActionsViews",params);
		System.out.println("Performing selectByActionsViews() ... ");
		return auditCount;
	}

	@SuppressWarnings("unchecked")
	public List<List<ApcaAuditCount>> selectByMonth(ApcaAuditQueryParameters params)
	{
		String[] dates = params.getSlicedDates().split(",");
		List<List<ApcaAuditCount>> auditCount= new ArrayList<List<ApcaAuditCount>>();
		for(int i=0 ; i < dates.length - 1 ; i++){
			params.setDateFrom(dates[i]);
			params.setDateTo(dates[i+1]);
			List<ApcaAuditCount> auditSample = new ArrayList<ApcaAuditCount>();
			auditSample = sqlMapClientTemplate.queryForList("alfresco.apca.audit.selectByMonth",params);
			auditCount.add(auditSample);
		}
		System.out.println("Performing selectByMonth() ... ");
		return auditCount;
	}

	@SuppressWarnings("unchecked")
	public List<List<ApcaAuditCount>> selectByDay(ApcaAuditQueryParameters params)
	{
		String[] dates = params.getSlicedDates().split(",");
		List<List<ApcaAuditCount>> auditCount= new ArrayList<List<ApcaAuditCount>>();
		for(int i=0 ; i < dates.length - 1 ; i++){
			params.setDateFrom(dates[i]);
			params.setDateTo(dates[i+1]);
			List<ApcaAuditCount> auditSample = new ArrayList<ApcaAuditCount>();
			auditSample = sqlMapClientTemplate.queryForList("alfresco.apca.audit.selectByDay",params);
			auditCount.add(auditSample);
		}
		System.out.println("Performing selectByDay() ... ");
		return auditCount;
	}
	public ApcaAuditQueryParameters buildParametersFromRequest(WebScriptRequest req)
	{
		try{
			//Users and object to define ...

			//Problème de long / null
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
		}
		catch(Exception e){
			System.out.println("Erreur lors de la construction des paramètres [select.java]");
			e.printStackTrace();
			return null;
		}
	}
}
