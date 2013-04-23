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