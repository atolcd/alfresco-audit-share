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

var userIsSiteManager = true;

if (page.url.templateArgs.site) {
  // We are in the context of a site, so call the repository to see if the user is site manager or not
  userIsSiteManager = false;

  var json = remote.call("/api/sites/" + page.url.templateArgs.site + "/memberships/" + encodeURIComponent(user.name));
  if (json.status == 200) {
    var obj = eval('(' + json + ')');
    if (obj) {
      userIsSiteManager = (obj.role == "SiteManager");
    }
  }
}

// Add the new component only for SiteManagers
if (userIsSiteManager) {
  model.pages.push(
    {
      title: "Site Statistics",
      pageUrl: "statistics/site-stats/",
      sitePageTitle: msg.get("link.site-stats")
    }
  );
}

model.userIsSiteManager = userIsSiteManager;