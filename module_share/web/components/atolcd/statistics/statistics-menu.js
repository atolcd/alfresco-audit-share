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

// AtolStatistics namespace
if (typeof AtolStatistics == undefined || !AtolStatistics) { var AtolStatistics = {}; AtolStatistics.module = AtolStatistics.module || {}; }

(function() {
  AtolStatistics.module.Menu = function(htmlId) {
    return AtolStatistics.module.Menu.superclass.constructor.call(this, "AtolStatistics.module.Menu", htmlId, ["button", "menu", "container"]);
  };

  YAHOO.extend(AtolStatistics.module.Menu, Alfresco.component.Base, {
    onReady: function Menu_onReady() {
      var container = YAHOO.util.Selector.query(".header .app-items")[0];
      var menu = ([
        { text : this.msg("header.global-usage.label"), classname: "global-usage", url : Alfresco.constants.URL_PAGECONTEXT + "statistics/share-stats/global-usage" },
        { text : this.msg("header.volumetry.label"), classname: "volumetry", url : Alfresco.constants.URL_PAGECONTEXT + "statistics/share-stats/volumetry" },
        { text : this.msg("header.user-connections.label"), classname: "user-connections", url : Alfresco.constants.URL_PAGECONTEXT + "statistics/share-stats/user-connections" }
       ]);

      if (this.options.isAdmin) {
        // add new menu entries
        menu.push([
            { text : this.msg("header.repo-usage.label"), classname: "global-usage", url : Alfresco.constants.URL_PAGECONTEXT + "statistics/share-stats/repo-usage" }
          ]
        );
      }

      this.widgets.menuButton = new YAHOO.widget.Button({
        id: container.id + "-menu",
        type: "menu",
        label: this.msg("link.site-stats"),
        menu: menu,
        lazyloadmenu: true,
        container: container.id
      });

      this.widgets.menuButton.addClass("statistics-menu");
    }
  });
})();