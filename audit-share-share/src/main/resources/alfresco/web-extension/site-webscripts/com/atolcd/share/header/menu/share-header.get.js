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

 // User needs to be administrator or SiteManger of at least one site
var showMenu = (user.isAdmin || auditshare.isSiteMangerOfAtLeastOneSite());
if (showMenu) {
  var auditShareHeaderMenu = {
    id: "HEADER_AUDIT_SHARE",
    name: "alfresco/header/AlfMenuBarPopup",
    config: {
      id: "HEADER_AUDIT_SHARE",
      label: "header.auditshare.label",
      widgets: [
        {
          name: "alfresco/menus/AlfMenuGroup",
          config: {
            label: "header.auditshare.group.sites.label",
            widgets: [
              {
                id: "HEADER_AUDIT_SHARE_SITE_GLOBAL_USAGE",
                name: "alfresco/header/AlfMenuItem",
                config: {
                  id: "HEADER_AUDIT_SHARE_SITE_GLOBAL_USAGE",
                  label: "header.global-usage.label",
                  iconClass: "global-usage",
                  targetUrl: "statistics/share-stats/global-usage"
                }
              },
              {
                id: "HEADER_AUDIT_SHARE_SITE_USER_CONNECTIONS",
                name: "alfresco/header/AlfMenuItem",
                config: {
                  id: "HEADER_AUDIT_SHARE_SITE_USER_CONNECTIONS",
                  label: "header.user-connections.label",
                  iconClass: "user-connections",
                  targetUrl: "statistics/share-stats/user-connections"
                }
              },
              {
                id: "HEADER_AUDIT_SHARE_SITE_VOLUMETRY",
                name: "alfresco/header/AlfMenuItem",
                config: {
                  id: "HEADER_AUDIT_SHARE_SITE_VOLUMETRY",
                  label: "header.volumetry.label",
                  iconClass: "volumetry",
                  targetUrl: "statistics/share-stats/volumetry"
                }
              }
            ]
          }
        }
      ]
    }
  };

  if (user.isAdmin) {
    auditShareHeaderMenu.config.widgets.push(
      {
        name: "alfresco/menus/AlfMenuGroup",
        config: {
          label: "header.auditshare.group.repository.label",
          widgets: [
            {
              id: "HEADER_AUDIT_SHARE_REPOSITORY_USAGE",
              name: "alfresco/header/AlfMenuItem",
              config: {
                id: "HEADER_AUDIT_SHARE_REPOSITORY_USAGE",
                label: "header.repo-usage.label",
                iconClass: "repo-usage",
                targetUrl: "statistics/share-stats/repo-usage"
              }
            }
          ]
        }
      }
    );

    auditShareHeaderMenu.config.widgets.push(
      {
        name: "alfresco/menus/AlfMenuGroup",
        config: {
          label: "header.auditshare.group.tools.label",
          widgets: [
            {
              id: "HEADER_AUDIT_SHARE_DATA_PURGE",
              name: "alfresco/header/AlfMenuItem",
              config: {
                id: "HEADER_AUDIT_SHARE_DATA_PURGE",
                label: "header.data-purge.label",
                iconClass: "data-purge",
                targetUrl: "statistics/share-stats/data-purge"
              }
            }
          ]
        }
      }
    );
  }

  var headerMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_APP_MENU_BAR");
  if (headerMenu) {
    headerMenu.config.widgets.push(auditShareHeaderMenu);
  }
}