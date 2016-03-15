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