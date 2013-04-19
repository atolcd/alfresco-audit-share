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
    Event = YAHOO.util.Event,
    Element = YAHOO.util.Element;

  /**
   * Alfresco Slingshot aliases
   */
  var $html = Alfresco.util.encodeHTML;

  /**
   * GlobalUsage constructor.
   *
   * @param {String} htmlId The HTML id üof the parent element
   * @return {AtolStatistics.GlobalUsage} The new GlobalUsage instance
   * @constructor
   */
  AtolStatistics.GlobalUsage = function GlobalUsage_constructor(htmlId) {
    AtolStatistics.GlobalUsage.superclass.constructor.call(this, "AtolStatistics.GlobalUsage", htmlId, ["button", "container", "json"]);

    // Surcharge de la classe Date. Récupère la semaine courante
    Date.prototype.getWeek = function() {
     var onejan = new Date(this.getFullYear(),0,1);
     return Math.ceil((((this - onejan) / 86400000) + onejan.getDay()+1)/7);
    };

    return this;
  };

  YAHOO.extend(AtolStatistics.GlobalUsage, Alfresco.component.Base, {
    /**
     * Cache-Résultat de la dernière requête exécutée
     * Utilisé pour l'export CSV
     */
    lastRequest: {
      params: null,
      data: null,
      from: null,
      to: null
    },

    /**
     * @attribute selectedSites
     * Tableau contenant tous les sites selectionnés dans la boîte de dialogue
     *
     */
    selectedSites: [],

    /**
     * @attribute siteDialog
     * Yahoo Simple Dialog - Boîte de dialogue permettant de
     * sélectionner un ou plusieurs sites
     *
     */
    siteDialog: null,

    /**
     * @attribute pathToSwf
     * Chemin vers le fichier swf d'Open Flash Chart
     *
     */
    pathToSwf: "/share/components/console/open_flash_chart/open-flash-chart.swf",

    /**
     * @attribute endDatesArray
     * Dates de référence utilisée pour les graphiques
     * Date présente par défaut
     */
    endDatesArray: [],
    /**
     * @attribute currentDateFilter
     * Filtre de date : days,weeks,months,years
     * "days" par défaut
     */
    currentDateFilter: "weeks",

    /**
     * @attribute sites
     * Informations sur les sites (id/titre).
     */
    sites: [],

    /**
     * @attribute limit
     * Limite de documents remontés par requête de popularité
     */
    limit: 5,

    /**
     * Fired by YUILoaderHelper when required component script files have
     * been loaded into the browser.
     *
     * @method onComponentsLoaded
     */
    onComponentsLoaded: function GlobalUsage_onComponentsLoaded() {
      Event.onContentReady(this.id, this.onReady, this, true);
    },

    /**
     * Fired by YUI when parent element is available for scripting.
     * Component initialisation, including instantiation of YUI widgets and event listener binding.
     *
     * @method onReady
     */
    onReady: function GlobalUsage_onReady() {
      // Buttons - Check ?
      // this.widgets.searchButton = Alfresco.util.createYUIButton(this, "search-button", this.onSearch);
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

      //Composants créé, on ajoute des listeners sur les menus.
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

      //el, sType, fn, obj, overrideContext
      Event.addListener("home", "click", this.onResetDates, null, this);
      Event.addListener("by-days", "click", this.onChangeDateFilter, { filter: "days" }, this);
      Event.addListener("by-weeks", "click", this.onChangeDateFilter, { filter: "weeks" }, this);
      Event.addListener("by-months", "click", this.onChangeDateFilter, { filter: "months" }, this);
      Event.addListener("by-years", "click", this.onChangeDateFilter, { filter: "years" }, this);

      Event.addListener("chart-prev", "click", this.onChangeDateInterval, { coef: -1 }, this);
      Event.addListener("chart-next", "click", this.onChangeDateInterval, { coef: 1 }, this);

      this.loadSites();
    },

    loadSites: function loadSites() {
      //Changement de style pour l'icône de chargement
      // this.widgets.siteButton.set("label", this.msg("label.loading") + ' <span class="loading"></span>');

      Alfresco.util.Ajax.jsonGet({
        url: Alfresco.constants.PROXY_URI + "share-stats/site/list-sites",
        successCallback: {
          fn: function (res) {
            this.createSiteMenu(res);
          },
          scope: this
        },
        failureMessage: this.msg("label.popup.error.list-site"),
        execScripts: true
      });
    },

    /**
     * @method createSiteDialog
     * @param res
     *
     */
    createSiteMenu: function GlobalUsage_createSiteDialog(res) {
      var menuButtons = [],
        current_site = null,
        sites = res.json,
        i = 0,
        ii = sites.length,
        me = this;

      var onSiteMenuClick = function (p_sType, p_aArgs, p_oItem) {
          var sText = p_oItem.cfg.getProperty("text"),
            value = p_oItem.value;

          me.widgets.siteButton.value = value;
          me.widgets.siteButton.set("label", sText);
          me.execute();
        };

      menuButtons.push({
        text: this.msg("label.menu.site.all"),
        value: "",
        onclick: {
          fn: onSiteMenuClick
        }
      });

      for (; i < ii; i++) {
        current_site = sites[i];
        menuButtons.push({
          text: current_site.title,
          value: current_site.name,
          onclick: {
            fn: onSiteMenuClick
          }
        });

        //Stockage des sites
        me.sites.push({
          name: current_site.name,
          title: current_site.title
        });
      }
      this.widgets.siteButton = new YAHOO.widget.Button({
        type: "split",
        name: "site-criteria",
        id: "site-criteria",
        menu: menuButtons,
        container: "criterias"
      });

      //Maj des infos du bouttons
      this.widgets.siteButton.set("label", this.msg("label.menu.site.all"));
      this.widgets.siteButton.value = "";

      this.execute();
    },

    onExport: function GlobalUsage_onExport() {
      if (this.lastRequest.params) {
        var params = this.lastRequest.params;
        params += "&interval=" + this.lastRequest.dateFilter;
        var url = Alfresco.constants.PROXY_URI + "share-stats/export-audits" + params;
        window.open(url);
      }
    },

    /**
     * @method
     * @param
     * @return
     */
    onSearch: function GlobalUsage_onSearch() {
      //Récupération des variables de l'UI
      var action = this.convertMenuValue(this.widgets.actionCriteriaButton.value),
        module = this.convertMenuValue(this.widgets.moduleCriteriaButton.value),
        dateFilter = this.currentDateFilter,
        site = this.convertMenuValue(this.widgets.siteButton.value),
        type = action,
        tsString = "";

      // Crétion du tableau d'intervalle de dates
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
      params = this.buildParams(module, site, null, type, from, to, this.limit);

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

          //Création du graphique Flash.
          swfobject.embedSWF(this.pathToSwf, id, additionalsParams.width, additionalsParams.height, "9.0.0", "expressInstall.swf", flashvars, params, attributes);
        }

      } else {
        //On remove le SWF courant.
        this.removeGraph(id);
        Dom.get(id).innerHTML = this.msg("message.no_results");
        this.widgets.exportButton.set("disabled", true);
      }
      // this.widgets.searchButton.blur();
    },

    /**
     * @method removeGraph
     * @return boolean
     */
    removeGraph: function GlobalUsage_removeGraph(id) {
      var swf = Dom.get(id),
        chartTag = swf.tagName.toLowerCase(),
        res = false;

      if (chartTag == "embed" || chartTag == "object") {
        swfobject.removeSWF(id);
        //Le conteneur étant détruit, il faut le recréer ...
        var newChartDiv = new YAHOO.util.Element(document.createElement("div"));
        newChartDiv.set("id", id);
        newChartDiv.appendTo(id + "-container");
        res = true;
      }

      return res;
    },


    /**
     * @method convertDate
     * @param d Date au format jj/mm/aaaa
     * @return integer Timestamp unix de la date
     */
    convertDate: function GlobalUsage_convertDate(d) {
      var res = 0;
      if (d.length > 0) {
        var dateArray = d.split('/');
        var dateToReturn = new Date(dateArray[2], dateArray[1] - 1, dateArray[0], 0, 0, 0);
        res = dateToReturn.getTime();
      }
      return res;
    },

    /**
     * @method convertTimeStamp
     * @param ts Timestamp unix
     * @param exclude boolean indiquant si le jour doit être exclu
     * @return string Date au format jj/mm/aaaa
     */
    convertTimeStamp: function GlobalUsage_convertTimeStamp(ts, exclude) {
      var d = new Date(ts);
      // retour un jour en arrière en cas d'exclude
      if (exclude) {
        d.setDate(d.getDate() - 1);
      }

      var month = (d.getMonth() + 1).toString(),
        day = d.getDate().toString(),
        year = d.getFullYear().toString();

      return day + "/" + month + "/" + year;
    },

    /**
     * Transforme les valeurs en cas de "" ou de undefined
     * @method convertMenuValue
     * @param val String Valeur du bouton
     * @return string Valeur "convertie"
     */
    convertMenuValue: function GlobalUsage_convertMenuValue(val) {
      var res = null;
      if (val !== undefined && val !== "") {
        res = val;
      }
      return res;
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
        params += "&site=" + site;
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

    /**
     * @method buildTimeStampArray Construit des intervalles de dates
     * @return array Tableau contenant les différents intervalles de dates
     */
    buildTimeStampArray: function GlobalUsage_buildTimeStampArray() {
      var tsArray = [],
        from = null,
        to = null,
        currentDay = null,
        next = null,
        hasNext = null,
        res = "";

      // Création de nouvelles dates à manipuler
      to = new Date(this.endDatesArray[this.currentDateFilter].getTime());
      from = new Date(this.endDatesArray[this.currentDateFilter].getTime());

      // Créé les intervalles allant du mois de départ au mois d'arrivée INCLUS
      if (this.currentDateFilter == "months") {
        tsArray.push(from.setDate(1));
        next = new Date(from);
        next.setDate(1);
        next.setDate(next.getDate() + 1);

        // Date d'arrêt
        to.setDate(1);
        to.setMonth(to.getMonth() + 1);

        hasNext = (to.getTime() > next.getTime());
        while (hasNext) {
          tsArray.push(next.getTime());
          next.setDate(next.getDate() + 1);
          hasNext = (to.getTime() > next.getTime());
        }
        tsArray.push(next.getTime());
      }
      // Selectionne par semaine suivant from et to.
      // Les semaines de "from" et "to" sont INCLUSES
      else if (this.currentDateFilter == "weeks") {
        //On utilise la date de départ pour récupérer tous les jours de la semaine
        next = null, currentDay = to.getDay(), hasNext = false;
        //Début de semaine
        from.setDate(to.getDate() - (currentDay - 1));
        next = new Date(from);
        tsArray.push(from.getTime());

        //Date d'arrêt
        to.setMonth(from.getMonth());
        to.setDate(from.getDate() + 7);

        next.setDate(from.getDate() + 1);
        hasNext = (to.getTime() > next.getTime());
        while (hasNext) {
          tsArray.push(next.getTime());
          next.setDate(next.getDate() + 1);
          hasNext = (to.getTime() > next.getTime());
        }
        //Semaine suivante, on test au cas où on dépasse.
        tsArray.push(next.getTime());
      }
      // Créé les intervalles allant du jour de départ au jour d'arrivée INCLUS
      else if (this.currentDateFilter == "days") {
        //On ajoute la date de départ
        tsArray.push(from.getTime());

        //On ajoute 1 jour à la date de fin, pour inclure le dernier jour selectionné.
        to.setDate(to.getDate() + 1);

        //On récupère le jour suivant
        next = new Date(from);
        next.setHours(next.getHours() + 2);

        //On vérifie qu'il ne dépasse pas la date de fin, on boucle
        hasNext = (to > next);
        while (hasNext) {
          tsArray.push(next.getTime());
          next.setHours(next.getHours() + 2);
          hasNext = (to > next);
        }
        tsArray.push(to.getTime());
      } else if (this.currentDateFilter == "years") {
        // On se place au début de l'année
        from.setDate(1);
        from.setMonth(0);
        tsArray.push(from.getTime());

        to.setDate(1);
        to.setMonth(0);
        to.setFullYear(to.getFullYear() + 1);

        next = new Date(from);
        next.setMonth(next.getMonth() + 1);
        hasNext = (to.getTime() > next.getTime());
        while (hasNext) {
          tsArray.push(next.getTime());
          next.setMonth(next.getMonth() + 1);
          hasNext = (to.getTime() > next.getTime());
        }
        tsArray.push(next.getTime());
      }

      return tsArray;
    },

    /**
     * @method onChangeDateFilter
     * @param e Event déclencheur
     * @param args Composant déclencheur
     * Gestionnaire click Jour / Semaine / Mois / Année
     */
    onChangeDateFilter: function GlobalUsage_OnChangeDateFilter(e, args) {
      if(e) Event.stopEvent(e);
      Dom.removeClass("by-" + this.currentDateFilter, "selected");
      Dom.addClass("by-" + args.filter, "selected");
      this.currentDateFilter = args.filter;
      this.execute();
    },


    /**
     * @method onChangeDateInterval
     * @param e Event déclencheur
     * @param args Composant déclencheur
     * Gestionnaire click suivant / précédent
     */
    onChangeDateInterval: function GlobalUsage_OnChangeDateInterval(e, args) {
      var coef = args.coef,
        currentDate = new Date(),
        dateFilter = this.currentDateFilter,
        newDate = new Date(this.endDatesArray[dateFilter]);

      Event.stopEvent(e);

      switch (dateFilter) {
      case "days":
        newDate.setDate(this.endDatesArray[dateFilter].getDate() + (1 * coef));
        break;
      case "weeks":
        newDate.setDate(this.endDatesArray[dateFilter].getDate() + (7 * coef));
        break;
      case "months":
        newDate.setMonth(this.endDatesArray[dateFilter].getMonth() + (1 * coef));
        break;
      case "years":
        newDate.setFullYear(this.endDatesArray[dateFilter].getFullYear() + (1 * coef));
        break;
      }

      this.endDatesArray[dateFilter] = newDate;
      this.execute();
    },

    onResetDates: function GlobalUsage_OnResetDates(){
      this.setupCurrentDates();
      this.execute();
    },

    execute: function GlobalUsage_execute() {
      this.getByPopularity("mostupdated");
      this.getByPopularity("mostread");
      this.onSearch();
    },

    setupCurrentDates : function GlobalUsage_setupCurrentDates(){
      var currentDate = new Date();
      currentDate.setMinutes(0);
      currentDate.setHours(0);
      currentDate.setMinutes(0);
      currentDate.setSeconds(0);
      currentDate.setMilliseconds(0);

      this.endDatesArray["days"] = currentDate;
      this.endDatesArray["weeks"] = currentDate;
      this.endDatesArray["months"] = currentDate;
      this.endDatesArray["years"] = currentDate;
    }
  });
})();