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
if (typeof AtolStatistics == undefined || !AtolStatistics) { var AtolStatistics = {}; }

/**
 * Volumetry tool component.
 *
 * @namespace AtolStatistics
 * @class AtolStatistics.Volumetry
 */
(function () {
  /**
   * YUI Library aliases
   */
  var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event;

  /**
   * Volumetry constructor.
   *
   * @param {String} htmlId The HTML id of the parent element
   * @return {AtolStatistics.Volumetry} The new Volumetry instance
   * @constructor
   */
  AtolStatistics.Volumetry = function Volumetry_constructor(htmlId) {
    AtolStatistics.Volumetry.superclass.constructor.call(this, "AtolStatistics.Volumetry", htmlId, ["button", "container", "json"]);
    return this;
  };

  YAHOO.extend(AtolStatistics.Volumetry, AtolStatistics.Tool, {
    /**
     * Fired by YUI when parent element is available for scripting.
     * Component initialisation, including instantiation of YUI widgets and event listener binding.
     *
     * @method onReady
     */
    onReady: function Volumetry_onReady() {
      AtolStatistics.Volumetry.superclass.onReady.call(this);

      var me = this;

      // Chart type button
      this.widgets.chartTypeCriteriaButton = new YAHOO.widget.Button(this.id + "-chart-type-criteria", {
        type: "split",
        menu: this.id + "-chart-type-criteria-select",
        lazyloadmenu: false
      });
      this.widgets.chartTypeCriteriaButton.value = "line";

      var onChartTypeMenuItemClick = function (p_sType, p_aArgs, p_oItem) {
        var sText = p_aArgs[1].cfg.getProperty("text"),
            value = p_aArgs[1].value;

        me.widgets.chartTypeCriteriaButton.value = value;
        me.widgets.chartTypeCriteriaButton.set("label", sText);
        me.execute();
      };
      this.widgets.chartTypeCriteriaButton.getMenu().subscribe("click", onChartTypeMenuItemClick);

      // el, sType, fn, obj, overrideContext
      Event.addListener(this.id + "-bar_stack-criteria", "click", this.onShowStackedBar, null, this);

      this.loadSites();
    },

    onCSVExport: function Volumetry_onCSVExport() {
      if (this.lastRequest.params) {
        var params = this.lastRequest.params;
        params += "&type=volumetry";
        params += "&values=" + this.lastRequest.values.toString();
        params += "&interval=" + this.lastRequest.dateFilter;
        var url = Alfresco.constants.PROXY_URI + "share-stats/export-audits" + params; // ?json=" + escape(YAHOO.lang.JSON.stringify(this.lastRequest.data));//JSON.stringify
        window.open(url);
      }
    },

    onSearch: function Volumetry_onSearch() {
      // Récupération des variables de l'UI
      var dateFilter = this.options.currentDateFilter,
          site = this.convertMenuValue(this.widgets.siteButton.value),
          tsString = "",
          params = "";

      // Création du tableau d'intervalle de dates
      if (dateFilter) {
        tsString = this.buildTimeStampArray().toString();
      }

      params = "?dates=" + tsString;
      if (site) {
        if (site.indexOf(',') >= 0) {
          params += "&sites=" + site;
        } else {
          params += "&site=" + site;
        }
      }

      // Création des paramètres et exécution de la requête
      this.lastRequest.params = params;
      this.lastRequest.dateFilter = dateFilter;

      var chartType = "line";
      if ((!site || site.indexOf(',') >= 0) && Dom.get(this.id + "-bar_stack-criteria").checked) {
        // multiple
        chartType = (this.widgets.chartTypeCriteriaButton.value == "bar") ? "bar_stack" : "lines";
      } else {
        // single
        chartType = (this.widgets.chartTypeCriteriaButton.value == "bar") ? "vbar" : "line";
      }

      var url = Alfresco.constants.PROXY_URI + "share-stats/select-volumetry" + this.lastRequest.params;
      Alfresco.util.Ajax.jsonGet({
        url: url,
        successCallback: {
          fn: this.displayGraph,
          scope: this
        },
        failureMessage: this.msg("label.popup.query.error"),
        execScripts: true,
        additionalsParams: {
          chartType: chartType,
          site: site,
          siteTitle: this.sites[site] || '',
          tsString: tsString,
          target: "chart",
          height: "450",
          width: "90%",
          chartId: this.id + '-chart'
        }
      });
    },

    onSiteMenuClick: function Volumetry_onSiteMenuClick(p_sType, p_aArgs, p_oItem) {
      var sText = p_oItem.cfg.getProperty("text"),
          value = p_oItem.value;

      if (value == "" || value.indexOf(',') >= 0) { // Tous les sites
        Dom.removeClass(this.id + "-bar-stack-criteria-container", "hidden");
      } else {
        Dom.addClass(this.id + "-bar-stack-criteria-container", "hidden");
      }

      this.widgets.siteButton.value = value;
      this.widgets.siteButton.set("label", sText);
      this.execute();
    },

    createSiteMenu: function Volumetry_createSiteMenu(res, hideAllSiteEntry) {
      if (this.options.siteId && this.options.siteId != "") {
        Dom.addClass(this.id + "-bar-stack-criteria-container", "hidden");
      }

      AtolStatistics.Volumetry.superclass.createSiteMenu.call(this, res, hideAllSiteEntry);
    },

    /**
     * @method displayGraph Affiche le requête suite à une requête Ajax
     * @param response Réponse de la requête
     */
    displayGraph: function Volumetry_displayGraph(response) {
      var additionalsParams, id, swf, chartTag;

      additionalsParams = response.config.additionalsParams;
      id = this.id + "-" + additionalsParams.target;
      swf = Dom.get(id);
      chartTag = swf.tagName.toLowerCase();

      if (response.json) {
        this.widgets.exportButton.set("disabled", false);
        this.lastRequest.values = response.json.values;

        response.json.currentFilter = this.options.currentDateFilter;
        response.json.additionalsParams = additionalsParams;

        if (chartTag == "embed" || chartTag == "object") {
          swf.load(getVolumetryFlashData(escape(YAHOO.lang.JSON.stringify(response.json))));
        } else {
          // Création variables et attribut - GetFlashData défini dans get_data.js - id : Variables json pour ofc.
          var flashvars = {
            "get-data": "getVolumetryFlashData",
            "id": escape(YAHOO.lang.JSON.stringify(response.json))
          },
            params = {
              wmode: "opaque"
            },
            // /!\ pour IE
            attributes = {
              salign: "l",
              AllowScriptAccess: "always"
            };

          // Création du graphique Flash.
          swfobject.embedSWF(this.options.pathToSwf, id, additionalsParams.width, additionalsParams.height, "9.0.0", "expressInstall.swf", flashvars, params, attributes);
        }

      } else {
        // On remove le SWF courant.
        this.removeGraph(id);
        Dom.get(id).innerHTML = this.msg("message.empty");
        this.widgets.exportButton.set("disabled", true);
      }
    },

    onShowStackedBar: function Volumetry_onShowStackedBar() {
      this.execute();
    }
  });
})();