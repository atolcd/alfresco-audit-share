package com.atolcd.alfresco.helper;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.Assert;

import com.atolcd.alfresco.web.scripts.shareStats.InsertAuditPost;

public class PermissionsHelper implements InitializingBean {

	private static SiteService siteService;
	private static AuthorityService authorityService;

	public void setSiteService(SiteService siteService) {
		PermissionsHelper.siteService = siteService;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		PermissionsHelper.authorityService = authorityService;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(siteService);
	}

	static public boolean isAuthorized(WebScriptRequest req) {
		String currentUser = AuthenticationUtil.getRunAsUser();

		// Alfresco administrators can access everything
		if (authorityService.isAdminAuthority(currentUser)) {
			return true;
		}

		Boolean isAllowed = false;

		String site = req.getParameter("site");
		if (site != null) {
			if (InsertAuditPost.SITE_REPOSITORY.equals(site)) {
				// mandatory: need to be Alfresco administrator
				return false;
			}

			// Current user must be "SiteManager" of the site
			isAllowed = isSiteManager(site, currentUser);
		}

		String sites = req.getParameter("sites");
		if (sites != null) {
			String[] sitesToken = sites.split(",");
			for (String token : sitesToken) {
				if (!isSiteManager(token, currentUser)) {
					return false;
				}
			}

			isAllowed = true;
		}

		return isAllowed;
	}

	private static boolean isSiteManager(String siteShortName, String userName) {
		try {
			String userRole = siteService.getMembersRole(siteShortName, userName);
			if (userRole != null && "SiteManager".equals(userRole)) {
				return true;
			}
		} catch (Exception e) {

		}
		return false;

	}
}
