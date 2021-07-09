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

function main() {
  // get the tool info from the request context
  var toolInfo = context.properties["statistic-tools"];

  // resolve the message labels
  for (var g = 0, group; g < toolInfo.length; g++) {
    group = toolInfo[g];
    for (var i = 0, info; i < group.length; i++) {
      info = group[i];
      info.label = msg.get(info.label);
      info.description = msg.get(info.description);
      if (info.group != "") {
        if (page.url.templateArgs.site) {
          info.groupLabel = info.groupLabel + ".site";
        }
        info.groupLabel = msg.get(info.groupLabel);
      }
    }
  }

  model.tools = toolInfo;
}

main();