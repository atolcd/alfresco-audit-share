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
    // Pour récupérer le titre de la discussion, on récupère le titre de l'enfant du même nom
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
  else if (componentId == "dataLists") {
    // TODO:
  }
  else if (componentId == "calendar") {
    // TODO:
  }

  return result;
}

model.result = main();