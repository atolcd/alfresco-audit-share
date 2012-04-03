package com.atolcd.alfresco.web.scripts.shareStats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
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

import com.atolcd.alfresco.AtolAuthorityParameters;
import com.atolcd.alfresco.AuditQueryParameters;

public class SelectUsersGet extends DeclarativeWebScript implements InitializingBean {
	private SqlMapClientTemplate sqlMapClientTemplate;
	private NodeService nodeService;
	private SiteService siteService;
	private AuthorityService authorityService;
	private long memberQnameId = 0;
	private long personQnameId = 0;
	private long containerQnameId = 0;

	private static final String SELECT_CONNECTED_USERS = "alfresco.atolcd.audit.selectConnectedUsers";
	private static final String SELECT_QNAME_ID = "alfresco.atolcd.audit.selectQNameId";
	private static final String SELECT_SITES_MEMBERS = "alfresco.atolcd.audit.selectSiteMember";

	// logger
	private static final Log logger = LogFactory.getLog(SelectUsersGet.class);

	public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate) {
		this.sqlMapClientTemplate = sqlMapClientTemplate;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.sqlMapClientTemplate);
		Assert.notNull(this.nodeService);
		Assert.notNull(this.siteService);

		memberQnameId = (Long) this.sqlMapClientTemplate.queryForObject(SELECT_QNAME_ID, "member");
		personQnameId = (Long) this.sqlMapClientTemplate.queryForObject(SELECT_QNAME_ID, "person");
		containerQnameId = (Long) this.sqlMapClientTemplate.queryForObject(SELECT_QNAME_ID, "authorityContainer");
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		try {
			Map<String, Object> model = new HashMap<String, Object>();
			if (this.sqlMapClientTemplate != null) {
				String type = req.getParameter("type");
				AuditQueryParameters auditQueryParameters = buildParametersFromRequest(req);
				if ("users-connected".equals(type) || "users-recently-connected".equals(type)) {
					model.put("users", selectConnectedUsers(auditQueryParameters));
				} else if ("users-count".equals(type)) {
					model.put("values", selectConnectedUsersByDate(auditQueryParameters));
				} else if ("users-never-connected".equals(type)) {
					AtolAuthorityParameters atolAuthorityParameters = buildAuthorityParametersFromRequest(req);
					model.put("users", selectNeverConnectedUsers(atolAuthorityParameters, auditQueryParameters));
				}
				model.put("type", type);
			}
			return model;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebScriptException("[ShareStats - SelectAudits] Error in executeImpl function");
		}
	}

	@SuppressWarnings("unchecked")
	public List<String> selectConnectedUsers(AuditQueryParameters params) {
		List<String> users = new ArrayList<String>();
		users = (List<String>) this.sqlMapClientTemplate.queryForList(SELECT_CONNECTED_USERS, params);
		return users;
	}

	@SuppressWarnings("unchecked")
	public int[] selectConnectedUsersByDate(AuditQueryParameters params) {
		String[] dates = params.getSlicedDates().split(",");
		int[] values = new int[dates.length - 1];
		for (int i = 0; i < dates.length - 1; i++) {
			params.setDateFrom(dates[i]);
			params.setDateTo(dates[i + 1]);
			List<String> users = new ArrayList<String>();
			users = sqlMapClientTemplate.queryForList(SELECT_CONNECTED_USERS, params);
			values[i] = users.size();
		}
		return values;
	}

	@SuppressWarnings("unchecked")
	public Set<String> selectNeverConnectedUsers(AtolAuthorityParameters atolAuthorityParameters, AuditQueryParameters auditQueryParameters) {
		List<String> users = new ArrayList<String>();
		
		List<String> groups = new ArrayList<String>();
		List<String> auditUsers = new ArrayList<String>();
		// Tous les membres de sites
		atolAuthorityParameters.setPersonQnameId(personQnameId);
		users = (List<String>) this.sqlMapClientTemplate.queryForList(SELECT_SITES_MEMBERS, atolAuthorityParameters);
		// Tous les groupes de sites
		atolAuthorityParameters.setPersonQnameId(containerQnameId);
		groups = (List<String>) this.sqlMapClientTemplate.queryForList(SELECT_SITES_MEMBERS, atolAuthorityParameters);
		// Tous les utilisateurs tracés par l'audit, en fonction des parametres
		auditUsers = (List<String>) this.sqlMapClientTemplate.queryForList(SELECT_CONNECTED_USERS, auditQueryParameters);

		Set<String>usersSet = new HashSet<String>(users.size());
		for (String group : groups) {
			if (group.startsWith("GROUP_")) {
				Set<String> s = authorityService.getContainedAuthorities(AuthorityType.USER, group, false);
				usersSet.addAll(s);
			}
		}

		// Differentiel
		usersSet.removeAll(auditUsers);
		return usersSet;
	}

	public AtolAuthorityParameters buildAuthorityParametersFromRequest(WebScriptRequest req) {
		try {
			AtolAuthorityParameters params = new AtolAuthorityParameters();
			String site = req.getParameter("site");
			if (site != null) {
				params.setSite(site);
			} else {
				// On liste tous les sites, puis on ajoute le nom du groupe
				// container
				List<SiteInfo> sitesInfo = siteService.listSites("", "");
				if (sitesInfo.size() > 0) {
					for (SiteInfo siteInfo : sitesInfo) {
						params.setSite(siteInfo.getShortName());
					}
				} else {
					params.setGroupNames(null);
				}
			}
			params.setMemberQnameId(this.memberQnameId);
			// siteService.listMembers("atol", "", "", 0, true);
			return params;
		} catch (Exception e) {
			logger.error("Erreur lors de la construction des parametres [buildAuthorityParametersFromRequest]");
			e.printStackTrace();
			return null;
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
