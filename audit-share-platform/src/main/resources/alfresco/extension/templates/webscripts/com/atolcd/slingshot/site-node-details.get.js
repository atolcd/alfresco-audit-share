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
  var siteId = url.templateArgs["siteId"];
  var componentId = url.templateArgs["componentId"];
  var objectId = url.templateArgs["objectId"];

  // Check parameters
  if (!siteId || !componentId || !objectId) {
    return status.setCode(status.STATUS_BAD_REQUEST, "No parameters supplied");
  }

  // Get the site
  var site = siteService.getSite(siteId);
  if (!site) {
    return status.setCode(status.STATUS_PRECONDITION_FAILED, "Could not find site: " + siteId);
  }

  // Result
  var result = {
    'nodeRef': '',
    'displayName': ''
  };

  // Get site container
  var container = site.getContainer(componentId);
  if (!container) {
    return status.setCode(status.STATUS_BAD_REQUEST, "Could not locate " + componentId + " container.");
  }

  if (componentId == "wiki" || componentId == "blog") {
    var page = container.childByNamePath(objectId);
    if (!page) {
      return status.setCode(status.STATUS_NOT_FOUND, "The page \"" + objectId.replace(/_/g, " ") + "\" does not exist.");
    }

    result.nodeRef = page.nodeRef.toString();
    result.displayName = page.properties["cm:title"] || page.name.replace(/_/g, " ");
  }
  else if (componentId == "discussions") {
    var discussion = container.childByNamePath(objectId);
    if (!discussion) {
      return status.setCode(status.STATUS_NOT_FOUND, "The discussion \"" + objectId + "\" does not exist.");
    }

    result.nodeRef = discussion.nodeRef.toString();
    result.displayName = discussion.childByNamePath(discussion.name).properties["cm:title"];
  }
  else if (componentId == "links") {
    var link = container.childByNamePath(objectId);
    if (!link) {
      return status.setCode(status.STATUS_NOT_FOUND, "The link \"" + objectId + "\" does not exist.");
    }

    result.nodeRef = link.nodeRef.toString();
    result.displayName = link.properties["lnk:title"] || [];
  }

  return result;
}

model.result = main();