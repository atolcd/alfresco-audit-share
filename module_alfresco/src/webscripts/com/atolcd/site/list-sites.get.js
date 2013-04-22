model.sites = siteService.listSites("","").sort(sortSite);
model.isAdmin = people.isAdmin(person);

function sortSite(a,b) {
  return (b.title.toLowerCase() > a.title.toLowerCase() ? -1 : 1);
}