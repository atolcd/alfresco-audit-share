/*
 * Copyright (C) 2018 Atol Conseils et Développements.
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

if (page.url.templateArgs.site != null && page.id != "search" && page.id != "advsearch") {
  if (auditshare.isMemberOfGroups("SiteManager")) {
    // AuditShare site page
    var statisticsPage = {
      id: "HEADER_SITE_STATISTICS",
      name: "alfresco/menus/AlfMenuBarItem",
      config: {
        id: "HEADER_SITE_STATISTICS",
        label: msg.get("link.site-stats"),
        targetUrl: "site/" + page.url.templateArgs.site + "/statistics/site-stats/",
        selected: ((page.titleId == "page.site.statistics.title"))
      }
    };

    var moreMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_SITE_MORE_PAGES");
    if (moreMenu) {
      // Add the page into the "More" menu
      moreMenu.config.widgets.push(statisticsPage);
    } else {
      var navigationMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_NAVIGATION_MENU_BAR");
      if (navigationMenu != null) {
        // Add the page into the "navigation bar"
        navigationMenu.config.widgets.push(statisticsPage);
      }
    }
  }
}