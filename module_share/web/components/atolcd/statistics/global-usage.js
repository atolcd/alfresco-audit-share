// AtolStatistics namespace
if (typeof AtolStatistics == undefined || !AtolStatistics) { var AtolStatistics = {}; }

/**
 * GlobalUsage tool component.
 *
 * @namespace AtolStatistics
 * @class AtolStatistics.GlobalUsage
 */
(function () {
  /**
   * YUI Library aliases
   */
  var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event;

  /**
   * GlobalUsage constructor.
   *
   * @param {String} htmlId The HTML id üof the parent element
   * @return {AtolStatistics.GlobalUsage} The new GlobalUsage instance
   * @constructor
   */
  AtolStatistics.GlobalUsage = function GlobalUsage_constructor(htmlId) {
    AtolStatistics.GlobalUsage.superclass.constructor.call(this, "AtolStatistics.GlobalUsage", htmlId, ["button", "container", "json"]);
    return this;
  };

  YAHOO.extend(AtolStatistics.GlobalUsage, AtolStatistics.Tool, {
    options: {
      /**
       * @attribute limit
       * Limite de documents remontés par requête de popularité
       */
      limit: 5
    },

    /**
     * Fired by YUI when parent element is available for scripting.
     * Component initialisation, including instantiation of YUI widgets and event listener binding.
     *
     * @method onReady
     */
    onReady: function GlobalUsage_onReady() {
      this.widgets.exportButton = Alfresco.util.createYUIButton(this, "export-button", this.onExport);
      this.widgets.exportButton.set("disabled", true);

      this.widgets.moduleCriteriaButton = new YAHOO.widget.Button("module-criteria", {
        type: "split",
        menu: "module-criteria-select",
        lazyloadmenu: false
      });
      this.widgets.moduleCriteriaButton.value = "document";

      this.widgets.actionCriteriaButton = new YAHOO.widget.Button("action-criteria", {
        type: "split",
        menu: "action-criteria-select",
        lazyloadmenu: false
      });
      this.widgets.actionCriteriaButton.value = "read";

      // Composants créé, on ajoute des listeners sur les menus.
      var me = this;
      // Comportement du menu de filtre par Modules
      var onModulesMenuItemClick = function (p_sType, p_aArgs, p_oItem) {
        var sText = p_aArgs[1].cfg.getProperty("text"),
            value = p_aArgs[1].value;

        me.widgets.moduleCriteriaButton.value = value;
        me.widgets.moduleCriteriaButton.set("label", sText);
        me.execute();
      };
      this.widgets.moduleCriteriaButton.getMenu().subscribe("click", onModulesMenuItemClick);

      // Comportement du menu de filtre par Actions
      var onActionsMenuItemClick = function (p_sType, p_aArgs, p_oItem) {
          var sText = p_aArgs[1].cfg.getProperty("text"),
            value = p_aArgs[1].value;

          me.widgets.actionCriteriaButton.value = value;
          me.widgets.actionCriteriaButton.set("label", sText);
          me.execute();
        };
      this.widgets.actionCriteriaButton.getMenu().subscribe("click", onActionsMenuItemClick);

      this.setupCurrentDates();

      // el, sType, fn, obj, overrideContext
      Event.addListener("home", "click", this.onResetDates, null, this);
      Event.addListener("by-days", "click", this.onChangeDateFilter, { filter: "days" }, this);
      Event.addListener("by-weeks", "click", this.onChangeDateFilter, { filter: "weeks" }, this);
      Event.addListener("by-months", "click", this.onChangeDateFilter, { filter: "months" }, this);
      Event.addListener("by-years", "click", this.onChangeDateFilter, { filter: "years" }, this);
      Event.addListener("chart-prev", "click", this.onChangeDateInterval, { coef: -1 }, this);
      Event.addListener("chart-next", "click", this.onChangeDateInterval, { coef: 1 }, this);

      this.loadSites();
    },

    onExport: function GlobalUsage_onExport() {
      if (this.lastRequest.params) {
        var params = this.lastRequest.params;
        params += "&interval=" + this.lastRequest.dateFilter;
        var url = Alfresco.constants.PROXY_URI + "share-stats/export-audits" + params;
        window.open(url);
      }
    },

    onSearch: function GlobalUsage_onSearch() {
      //Récupération des variables de l'UI
      var action = this.convertMenuValue(this.widgets.actionCriteriaButton.value),
          module = this.convertMenuValue(this.widgets.moduleCriteriaButton.value),
          dateFilter = this.currentDateFilter,
          site = this.convertMenuValue(this.widgets.siteButton.value),
          type = action,
          tsString = "";

      // Création du tableau d'intervalle de dates
      if (dateFilter) {
        tsString = this.buildTimeStampArray().toString();
      }

      // Création des paramètres et exécution de la requête
      this.lastRequest.params = this.buildParams(module, site, tsString, type);
      this.lastRequest.dateFilter = dateFilter;

      var url = Alfresco.constants.PROXY_URI + "share-stats/select-audits" + this.lastRequest.params;
      Alfresco.util.Ajax.jsonGet({
        url: url,
        successCallback: {
          fn: this.displayGraph,
          scope: this
        },
        failureMessage: this.msg("label.popup.query.error"),
        execScripts: true,
        additionalsParams: {
          chartType: "vbar",
          type: type,
          tsString: tsString,
          target: "chart",
          height: "450",
          width: "90%"
        }
      });

      // Problème de focus avec le bouton et flash
      // this.widgets.searchButton.blur();
    },

    getByPopularity: function GlobalUsage_getByPopularity(type) {
      var site = this.convertMenuValue(this.widgets.siteButton.value),
          module = this.convertMenuValue(this.widgets.moduleCriteriaButton.value),
          dateFilter = this.currentDateFilter,
          tsArray = this.buildTimeStampArray(),
          from = tsArray[0],
          to = tsArray[tsArray.length - 1],
          params = null;

      // Création des paramètres et exécution de la requête
      params = this.buildParams(module, site, null, type, from, to, this.options.limit);

      var url = Alfresco.constants.PROXY_URI + "share-stats/select-audits" + params;
      Alfresco.util.Ajax.jsonGet({
        url: url,
        successCallback: {
          fn: this.displayGraph,
          scope: this
        },
        failureMessage: this.msg("label.popup.query.error"),
        execScripts: true,
        additionalsParams: {
          chartType: "hbar",
          type: type,
          target: type,
          height: "200",
          width: "100%",
          from: from,
          to: to,
          module: module,
          urlTemplate : this.getTemplateUrl(module)
        }
      });
    },

    getTemplateUrl: function GlobalUsage_getTemplateUrl(module) {
      var baseUrl = window.location.protocol + "//" + window.location.host + Alfresco.constants.URL_PAGECONTEXT + "site/{site}/";
      if (module == "document") {
        return baseUrl + "document-details?nodeRef={nodeRef}";
      } else if (module == "wiki") {
        return baseUrl + "wiki-page?title={id}&listViewLinkBack=true";
      } else if (module == "blog") {
        return baseUrl + "blog-postview?postId={id}&listViewLinkBack=true";
      } else if (module == "discussions") {
        return baseUrl + "discussions-topicview?topicId={id}&listViewLinkBack=true";
      }
      return "";
    },

    /**
     * @method displayGraph Affiche le requête suite à une requête Ajax
     * @param response Réponse de la requête
     */
    displayGraph: function GlobalUsage_displayGraph(response) {
      var additionalsParams, id, swf, chartTag;

      additionalsParams = response.config.additionalsParams;
      id = this.id + "-" + additionalsParams.target;
      swf = Dom.get(id);
      chartTag = swf.tagName.toLowerCase();

      if (response.json) {
        this.widgets.exportButton.set("disabled", false);
        response.json.currentFilter = this.currentDateFilter;
        response.json.additionalsParams = additionalsParams;
        // response.json.currentSites = this.sites;
        // console.log(getFlashData(escape(YAHOO.lang.JSON.stringify(response.json))));

        if (chartTag == "embed" || chartTag == "object") {
          swf.load(getFlashData(escape(YAHOO.lang.JSON.stringify(response.json))));
        } else {
          //Création variables et attribut - GetFlashData défini dans get_data.js - id : Variables json pour ofc.
          var flashvars = {
            "get-data": "getFlashData",
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
      // this.widgets.searchButton.blur();
    },

    /**
     * @method buildParams Construit une chaîne de caractère pour passer les arguments en GET
     * @param from Timestamp unix (string) de la date minimum
     * @param to Timestamp unix (string) de la date maximum
     * @param action Action selectionnée dans l'UI  --> Useless ?
     * @param module Module selectionné dans l'UI
     * @param dates Ensemble des tranches de dates dans le cas d'une recherche par date
     * @param type Type de requête à effectuer
     * @param limit Limite de résultats

     * @return string params argument à passer à la requête
     */
    buildParams: function GlobalUsage_buildParams(module, site, dates, type, from, to, limit) {
      var params = "?type=" + type;

      if (dates !== null && dates != "") {
        params += "&dates=" + dates;
      }
      if (module !== null) {
        params += "&module=" + module;
      }
      if (site !== null) {
        if (site.indexOf(',') >= 0) {
          params += "&sites=" + site;
        } else {
          params += "&site=" + site;
        }
      }
      if (from) {
        params += "&from=" + from;
      }
      if (to) {
        params += "&to=" + to;
      }
      if (limit) {
        params += "&limit=" + limit;
      }
      return params;
    },

    execute: function GlobalUsage_execute() {
      this.getByPopularity("mostupdated");
      this.getByPopularity("mostread");

      AtolStatistics.GlobalUsage.superclass.execute.call(this);
    }
  });
})();