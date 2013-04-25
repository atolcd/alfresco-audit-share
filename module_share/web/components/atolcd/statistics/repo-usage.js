// AtolStatistics namespace
if (typeof AtolStatistics == undefined || !AtolStatistics) { var AtolStatistics = {}; }

/**
 * RepoUsage tool component.
 *
 * @namespace AtolStatistics
 * @class AtolStatistics.RepoUsage
 */
(function () {
  /**
   * YUI Library aliases
   */
  var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event;

  /**
   * RepoUsage constructor.
   *
   * @param {String} htmlId The HTML id of the parent element
   * @return {AtolStatistics.RepoUsage} The new RepoUsage instance
   * @constructor
   */
  AtolStatistics.RepoUsage = function RepoUsage_constructor(htmlId) {
    AtolStatistics.RepoUsage.superclass.constructor.apply(this, arguments);
    return this;
  };

  YAHOO.extend(AtolStatistics.RepoUsage, AtolStatistics.GlobalUsage, {
    onReady: function RepoUsage_onReady() {
      AtolStatistics.RepoUsage.superclass.onReady.call(this);

      // site button
      this.widgets.siteButton = new YAHOO.widget.Button("site-criteria", {
        type: "split",
        menu: "site-criteria-select",
        lazyloadmenu: false
      });

      // set the '_repository' value
      this.widgets.siteButton.value = "_repository";

      // hide the button
      this.widgets.siteButton.addClass("hidden");

      // disabled the "module" button (there is only one choice)
      this.widgets.moduleCriteriaButton.set("disabled", true);

      this.execute();
    },

    loadSites: function RepoUsage_loadSites() {
      // Nothing to do
    },

    getTemplateUrl: function RepoUsage_getTemplateUrl(module) {
      // Repository url
      return window.location.protocol + "//" + window.location.host + Alfresco.constants.URL_PAGECONTEXT + "document-details?nodeRef={nodeRef}";
    }
  });
})();