var userIsSiteManager = true;

if (page.url.templateArgs.site) {
  // We are in the context of a site, so call the repository to see if the user is site manager or not
  userIsSiteManager = false;

  var json = remote.call("/api/sites/" + page.url.templateArgs.site + "/memberships/" + encodeURIComponent(user.name));
  if (json.status == 200) {
    var obj = eval('(' + json + ')');
    if (obj) {
      userIsSiteManager = (obj.role == "SiteManager");
    }
  }
}

// Add the new component only for SiteManagers
if (userIsSiteManager) {
  model.pages.push(
    {
      title: "Site Statistics",
      pageUrl: "site-statistics/site-stats/",
      sitePageTitle: msg.get("link.site-stats")
    }
  );
}

model.userIsSiteManager = userIsSiteManager;