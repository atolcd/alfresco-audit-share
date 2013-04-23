function main() {
  var toolInfo = {}; // return an map of group->tool[] information
  var currentToolId = page.url.templateArgs["toolid"]; // the current tool may have been specified on the URL

  // family of tools to use for this console is linked to the current pageId from the URL
  var family = page.url.templateArgs["pageid"];
  if (family != null) {
    model.userIsAllowed = checkPermissions();
    if (!model.userIsAllowed) {
      return
    }

    var component = sitedata.getComponent("page", "myctool", family); // find the existing current tool component binding

    // collect the tools required for this console
    var tools = sitedata.findWebScripts(family);

    // collect the tools reserved for admins ("admin-stats" family)
    if (user.isAdmin && !page.url.templateArgs.site) {
      tools = tools.concat(sitedata.findWebScripts("admin-stats"));
    }

    // process each tool and generate the data so that a label+link can
    // be output by the component template for each tool required
    for (var i=0, ii=tools.length ; i<ii; i++) {
      var tool = tools[i],
        id = tool.id,
        scriptName = id.substring(id.lastIndexOf('/') + 1, id.lastIndexOf('.')),
        toolUrl = (new String(tool.getURIs()[0])).toString();

      // handle the case when no tool selection in the URL - select the first
      if (!currentToolId || currentToolId.length == 0) {
        currentToolId = scriptName;
      }

      // use the webscript ID to generate message bundle IDs
      var labelId = "tool." + scriptName + ".label",
          descId = "tool." + scriptName + ".description";

      // identify statistics tool grouping if any
      // simple convention is used to resolve group - last element of the webscript package path after 'statistics'
      // for example: org.alfresco.components.statistics.repository = repository
      //              org.yourcompany.statistics.mygroup = mygroup
      // package paths not matching the convention will be placed in the default root group
      // the I18N label should be named: tool.group.<yourgroupid>
      var group = "",
          groupLabelId = null,
          paths = tool.scriptPath.split('/');

      if (paths.length > 4 && paths[3] == "statistics") {
        // found webscript package grouping
        group = paths[4];
        groupLabelId = "tool.group." + group;
      }

      var info = {
        id: scriptName,
        url: toolUrl,
        label: labelId,
        group: group,
        groupLabel: groupLabelId,
        description: descId,
        selected: (currentToolId == scriptName)
      };

      // generate the tool info structure for template usage
      if (!toolInfo[group]) {
        // add initial group structure
        toolInfo[group] = [];
      }
      toolInfo[group].push(info);

      // dynamically update the component binding if this tool is the current selection
      if (info.selected) {
        if (component == null) {
          // first ever visit to the page - there is no component binding yet
          component = sitedata.newComponent("page", "myctool", family);
        }

        if (component.properties.url != toolUrl) {
          component.properties.url = toolUrl;
          component.save(false);
        }
      }
    }
  }

  // Save the tool info structure into the request context, it is used
  // downstream by the statistic-tools component to dynamically render tool links.
  // Processing is performed here as the component binding must be set before rendering begins!
  var toolsArray = [];
  for each (var g in toolInfo) {
    toolsArray.push(g);
  }
  context.setValue("statistic-tools", toolsArray);
}

function checkPermissions() {
  // Check permissions
  if (page.url.templateArgs.site) {
    // We are in the context of a site, so call the repository to see if the user is site manager or not
    var json = remote.call("/api/sites/" + page.url.templateArgs.site + "/memberships/" + encodeURIComponent(user.name));
    if (json.status == 200) {
      var obj = eval('(' + json + ')');
      if (obj) {
        return (obj.role == "SiteManager");
      }
    }
  } else if (user.isAdmin) {
    // User is an administrator
    return true;
  } else {
    // Check if current user is site manager of a site
    // http://localhost:8080/alfresco/s/api/people/admin/sites?roles=user
    var json = remote.call("/api/people/" + encodeURIComponent(user.name) + "/sites?roles=user");
    if (json.status == 200) {
      var sites = eval('(' + json + ')');
      if (sites && sites.length > 0) {
        for (var i=0, ii=sites.length ; i<ii ; i++) {
          if (sites[i].siteRole == "SiteManager") {
            return true;
          }
        }
      }
    }
  }
  return false;
}

main();