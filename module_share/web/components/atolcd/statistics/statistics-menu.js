// AtolStatistics namespace
if (typeof AtolStatistics == undefined || !AtolStatistics) { var AtolStatistics = {}; AtolStatistics.module = AtolStatistics.module || {}; }

(function() {
  AtolStatistics.module.Menu = function(htmlId) {
    return AtolStatistics.module.Menu.superclass.constructor.call(this, "AtolStatistics.module.Menu", htmlId, ["button", "menu", "container"]);
  };

  YAHOO.extend(AtolStatistics.module.Menu, Alfresco.component.Base, {
    onReady: function Menu_onReady() {
      var container = YAHOO.util.Selector.query(".header .app-items")[0];
      var menu = ([
        { text : this.msg("header.global-usage.label"), title: "TEST", classname: "global-usage", url : Alfresco.constants.URL_PAGECONTEXT + "statistics/share-stats/global-usage" },
        { text : this.msg("header.volumetry.label"), classname: "volumetry", url : Alfresco.constants.URL_PAGECONTEXT + "statistics/share-stats/volumetry" },
        { text : this.msg("header.user-connections.label"), classname: "user-connections", url : Alfresco.constants.URL_PAGECONTEXT + "statistics/share-stats/user-connections" }
       ]);

      if (this.options.isAdmin) {
        // add new menu entries
      }

      this.widgets.menuButton = new YAHOO.widget.Button({
        id: container.id + "-menu",
        type: "menu",
        label: this.msg("link.site-stats"),
        menu: menu,
        lazyloadmenu: true,
        container: container.id
      });

      this.widgets.menuButton.addClass("statistics-menu");
    }
  });
})();