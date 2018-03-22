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

const refGroup = "auditshare-user-connections-groups";
const groupPrefix = "GROUP_";

function main() {
  try {
    var groups = sharestats.getReferentiel(refGroup);

    // Only if the group exists, check if it have a label and assign it the
    // default group label if it does not exist.
    for (var i = 0, ii = groups.size(); i < ii; i++) {
      var groupObj = groups.get(i),
          group = people.getGroup(groupObj.getId());
      if (group) {
        if (groupObj.getLibelle() == null || (groupObj.getLibelle() != null && groupObj.getLibelle().isEmpty())) {
          var groupDefaultName = (group.properties["cm:authorityDisplayName"]) ? group.properties["cm:authorityDisplayName"]
              : group.properties["cm:authorityName"].substring(groupPrefix.length);
          groupObj.setLibelle(groupDefaultName);
        }
        model.items.push(groupObj);
      } else {
        logger.log("The group " + groupObj.getId() + " does not exist.");
      }
    }
  } catch (e) {
    logger.log("An error occurred while retrieving user groups: " + e);
  }
}

model.items = [];
main();