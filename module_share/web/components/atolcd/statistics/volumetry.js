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
      this.setupCurrentDates();

      // Buttons - Check ?
      this.widgets.exportButton = Alfresco.util.createYUIButton(this, "export-button", this.onExport);
      this.widgets.exportButton.set("disabled", true);

      // Chart type button
      this.widgets.chartTypeCriteriaButton = new YAHOO.widget.Button("chart-type-criteria", {
        type: "split",
        menu: "chart-type-criteria-select",
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
      Event.addListener("bar_stack-criteria", "click", this.onShowStackedBar, null, this);
      Event.addListener("home", "click", this.onResetDates, null, this);
      Event.addListener(this.id + "-by-days", "click", this.onChangeDateFilter, { filter: "days" }, this);
      Event.addListener(this.id + "-by-weeks", "click", this.onChangeDateFilter, { filter: "weeks" }, this);
      Event.addListener(this.id + "-by-months", "click", this.onChangeDateFilter, { filter: "months" }, this);
      Event.addListener(this.id + "-by-years", "click", this.onChangeDateFilter, { filter: "years" }, this);
      Event.addListener("chart-prev", "click", this.onChangeDateInterval, { coef: -1 }, this);
      Event.addListener("chart-next", "click", this.onChangeDateInterval, { coef: 1 }, this);

      this.loadSites();
    },

    onExport: function Volumetry_onExport() {
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
      if ((!site || site.indexOf(',') >= 0) && Dom.get("bar_stack-criteria").checked) {
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
          width: "90%"
        }
      });
    },

    onSiteMenuClick: function Volumetry_onSiteMenuClick(p_sType, p_aArgs, p_oItem) {
      var sText = p_oItem.cfg.getProperty("text"),
          value = p_oItem.value;

      if (value == "" || value.indexOf(',') >= 0) { // Tous les sites
        Dom.removeClass("bar-stack-criteria-container", "hidden");
      } else {
        Dom.addClass("bar-stack-criteria-container", "hidden");
      }

      this.widgets.siteButton.value = value;
      this.widgets.siteButton.set("label", sText);
      this.execute();
    },

    createSiteMenu: function Volumetry_createSiteMenu(res, hideAllSiteEntry) {
      if (this.options.siteId && this.options.siteId != "") {
        Dom.addClass("bar-stack-criteria-container", "hidden");
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