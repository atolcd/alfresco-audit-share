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

var showMenu = false;

if (user.isAdmin) {
  showMenu = true;
} else {
  // user needs to be SiteManger of at least one site
  var json = remote.call("/share-stats/site/list-sites?role=SiteManager");
  if (json.status == 200) {
    var sites = eval('(' + json + ')');
    if (sites && sites.length > 0) {
      showMenu = true;
    }
  }
}

model.showMenu = showMenu;