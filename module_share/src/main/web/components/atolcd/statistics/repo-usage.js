/*
 * Copyright (C) 2013 Atol Conseils et Développements.
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

// AtolStatistics namespace
if (typeof AtolStatistics == "undefined" || !AtolStatistics) { var AtolStatistics = {}; }

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
      var repoExecute = AtolStatistics.RepoUsage.superclass.onReady.call(this);

      if (!repoExecute) {
        return;
      }
      // site button
      this.widgets.siteButton = new YAHOO.widget.Button(this.id + "-site-criteria", {
        type: "split",
        menu: this.id + "-site-criteria-select",
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
    }
  });
})();