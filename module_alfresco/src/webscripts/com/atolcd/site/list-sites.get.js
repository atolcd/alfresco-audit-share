model.sites = siteService.listSites("","").sort(sortSite);

function sortSite(a,b) {
  return (b.title.toLowerCase() > a.title.toLowerCase() ? -1 : 1);
}