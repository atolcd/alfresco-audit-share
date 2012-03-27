/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * ConsoleAudit tool component.
 *
 * @namespace Alfresco
 * @class Alfresco.ConsoleAudit
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
   * ConsoleAudit constructor.
   *
   * @param {String} htmlId The HTML id üof the parent element
   * @return {Alfresco.ConsoleAudit} The new ConsoleAudit instance
   * @constructor
   */
  Alfresco.ConsoleAudit = function (htmlId) {
    this.name = "Alfresco.ConsoleAudit";
    Alfresco.ConsoleAudit.superclass.constructor.call(this, htmlId);

    /* Register this component */
    Alfresco.util.ComponentManager.register(this);

    /* Load YUI Components */
    Alfresco.util.YUILoaderHelper.require(["button", "container", "datasource", "paginator", "json", "history"], this.onComponentsLoaded, this);

    /* Define panel handlers */
    var parent = this;

    // NOTE: the panel registered first is considered the "default" view and is displayed first
    /* Audit Panel Handler */
    AuditPanelHandler = function AuditPanelHandler_constructor() {
      AuditPanelHandler.superclass.constructor.call(this, "audit");
    };

    YAHOO.extend(AuditPanelHandler, Alfresco.ConsolePanelHandler, {
      /**
       * Called by the ConsolePanelHandler when this panel shall be loaded
       *
       * @method onLoad
       */
      onLoad: function onLoad() {
        // Buttons - Check ?
        parent.widgets.searchButton = Alfresco.util.createYUIButton(parent, "search-button", parent.onSearch);
        parent.widgets.exportButton = Alfresco.util.createYUIButton(parent, "export-button", parent.onExport);
        parent.widgets.siteButton = Alfresco.util.createYUIButton(parent, "site-button", parent.onShowSites);

        parent.widgets.exportButton.set("disabled", true);
        parent.widgets.siteButton.set("disabled", true);

        parent.widgets.moduleCriteriaButton = new YAHOO.widget.Button("module-criteria", {
          type: "split",
          menu: "module-criteria-select",
          lazyloadmenu: false
        });

        parent.widgets.actionCriteriaButton = new YAHOO.widget.Button("action-criteria", {
          type: "split",
          menu: "action-criteria-select",
          lazyloadmenu: false
        });

        //Par défaut
        parent.widgets.moduleCriteriaButton.value = "wiki";
        parent.widgets.actionCriteriaButton.value = "views";

        //el, sType, fn, obj, overrideContext
        Event.addListener("by-days", "click", parent.onChangeDateFilter, {
          filter: "days"
        }, parent);
        Event.addListener("by-weeks", "click", parent.onChangeDateFilter, {
          filter: "weeks"
        }, parent);
        Event.addListener("by-months", "click", parent.onChangeDateFilter, {
          filter: "months"
        }, parent);
        Event.addListener("by-years", "click", parent.onChangeDateFilter, {
          filter: "years"
        }, parent);

        Event.addListener("by-previous", "click", parent.onChangeDateInterval, {
          interval: "previous"
        }, parent);
        Event.addListener("by-next", "click", parent.onChangeDateInterval, {
          interval: "next"
        }, parent);
        //Tableau global pour les sites. Nécessaire pour l'appel à la fonction du embedSWF.
        GLOBALS_sites = [];
        this.createSiteDialog();
      },

      /**
       * Création de la boîte de dialogue de sélection des sites
       * à partir du résultat du WebScript.
       * @method createSiteDialog
       *
       */
      createSiteDialog: function createSiteDialog() {
        //Changement de style pour l'icône de chargement
        parent.widgets.siteButton.set("label", parent._msg("label.loading") + ' <span class="loading"></span>');

        Alfresco.util.Ajax.jsonGet({
          url: Alfresco.constants.PROXY_URI + "share-stats/site/list-sites",
          successCallback: {
            fn: function (res) {
              this.createSiteDialog(res);
            },
            scope: parent
          },
          failureMessage: parent._msg("label.popup.error.list-site"),
          execScripts: true
        });
      }
    });

    new AuditPanelHandler();

    return this;
  };

  YAHOO.extend(Alfresco.ConsoleAudit, Alfresco.ConsoleTool, {

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
     * Fired by YUILoaderHelper when required component script files have
     * been loaded into the browser.
     *
     * @method onComponentsLoaded
     */
    onComponentsLoaded: function ConsoleAudit_onComponentsLoaded() {
      Event.onContentReady(this.id, this.onReady, this, true);
    },

    /**
     * Fired by YUI when parent element is available for scripting.
     * Component initialisation, including instantiation of YUI widgets and event listener binding.
     *
     * @method onReady
     */
    onReady: function ConsoleAudit_onReady() {
      // Call super-class onReady() method
      Alfresco.ConsoleAudit.superclass.onReady.call(this);

      //Composants créé, on ajoute des listeners sur les menus.
      var me = this;
      // Comportement du menu de filtre par Modules
      var onModulesMenuItemClick = function (p_sType, p_aArgs, p_oItem) {
          var sText = p_aArgs[1].cfg.getProperty("text"),
            value = p_aArgs[1].value;

          me.widgets.moduleCriteriaButton.value = value;
          me.widgets.moduleCriteriaButton.set("label", sText);
        };
      this.widgets.moduleCriteriaButton.getMenu().subscribe("click", onModulesMenuItemClick);

      // Comportement du menu de filtre par Actions
      var onActionsMenuItemClick = function (p_sType, p_aArgs, p_oItem) {
          var sText = p_aArgs[1].cfg.getProperty("text"),
            value = p_aArgs[1].value;

          me.widgets.actionCriteriaButton.value = value;
          me.widgets.actionCriteriaButton.set("label", sText);
        };
      this.widgets.actionCriteriaButton.getMenu().subscribe("click", onActionsMenuItemClick);

      var currentDate = new Date();
      currentDate.setMinutes(0);
      currentDate.setHours(0);
      currentDate.setMinutes(0);
      currentDate.setSeconds(0);

      this.endDatesArray["days"] = currentDate; //this.currentEndDate
      this.endDatesArray["weeks"] = currentDate; //this.endDatesArray[dateFilter]
      this.endDatesArray["months"] = currentDate;
      this.endDatesArray["years"] = currentDate;
    },

    /**
     * @method createSiteDialog
     * @param res
     *
     */
    createSiteDialog: function ConsoleAudit_createSiteDialog(res) {
      this.siteDialog = new YAHOO.widget.SimpleDialog(this.id + "-siteDialog", {
        width: "54em",
        effect: {
          effect: YAHOO.widget.ContainerEffect.FADE,
          duration: 0.25
        },
        fixedcenter: true,
        modal: true,
        visible: false,
        draggable: true

      });


      //Config des boutons Ok-Clean
      var cleanDialog = function (e) {
          me.cleanSiteDialog(this);
        };

      var writeSelectedSites = function (e) {
          var sitesCount = me.selectedSites.length > 10 ? 10 : me.selectedSites.length,
            sitesToString = " | ";

          if (sitesCount > 0) {

            for (var i = 0; i < sitesCount; i++) {
              sitesToString += me.selectedSites[i].title + ' | ';
            }

            if (me.selectedSites.length > 10) {
              sitesToString += " ...";
            }
            Dom.get("choosen-sites").innerHTML = sitesToString;
            Dom.setStyle("choosen-sites-container", 'display', 'block');
          } else {
            Dom.setStyle("choosen-sites-container", 'display', 'none');
          }

          me.siteDialog.hide();
        };

      var myButtons = [{
        text: this.msg("button.ok"),
        handler: writeSelectedSites,
      }, {
        text: this.msg("button.clean"),
        handler: cleanDialog
      }];

      this.siteDialog.cfg.queueProperty("buttons", myButtons);
      this.siteDialog.render(document.body);


      this.siteDialog.setHeader(this.msg("button.choose"));
      this.siteDialog.setBody('<div class="siteDialog" id="' + this.siteDialog.id + '-site-list"><div id="invisible-button-container"></div><div id="button-container"></div><div id="siteDialog-paginator"></div></div>');

      this.siteDialog.buttons = [];
      var me = this,
        sitesPerPage = 50,
        container = null,
        checkButton = null;

      //Création des boutons - Pas de buttonGroup
      for (var i = 0, ii = res.json.length; i < ii; i++) {
        opt = res.json[i];
        optid = this.siteDialog.id + "-site-list" + i;

        container = i > sitesPerPage - 1 ? "invisible-button-container" : "button-container";
        checkButton = new YAHOO.widget.Button({
          id: optid,
          name: opt.name,
          label: '<em class="em-site-button">' + opt.title + '</em>',
          value: opt.name,
          type: "checkbox",
          title: opt.title,
          container: container,
          onclick: {
            fn: function () {
              var siteName = this.get("value"),
                title = this.get("title");
              if (this.get("checked")) {
                me.selectedSites.push({
                  "shortName": siteName,
                  "title": title
                });
              } else {
                for (var i = 0, ii = me.selectedSites.length; i < ii; i++) {
                  if (me.selectedSites[i].shortName == siteName) {
                    me.selectedSites.splice(i, 1);
                    break;
                  }
                }
              }
            },
            obj: me
          }
        });
        //Stockage du button pour le réutiliser plus rapidement ensuite
        this.siteDialog.buttons.push(checkButton);
        //Stockage des sites
        GLOBALS_sites.push({
          name: opt.name,
          title: opt.title
        });
      }

      this.siteDialog.pager = new YAHOO.widget.Paginator({
        rowsPerPage: 1,
        totalRecords: res.json.length / sitesPerPage,
        containers: "siteDialog-paginator",
        alwaysVisible: false,
        pageLinks: 20,
        template: "{PageLinks}"
      });

      var handlePagination = function (state) {
          // Show the appropriate content for the requested page
          var startOffset = (state.page - 1) * sitesPerPage,
            endOffset = state.page * sitesPerPage,
            oldStartOffset = (state.before.page - 1) * sitesPerPage,
            oldEndOffset = state.before.page * sitesPerPage;

          for (var i = oldStartOffset; i < oldEndOffset; i++) {
            if (me.siteDialog.buttons[i] != null) {
              me.siteDialog.buttons[i].appendTo("invisible-button-container");
            }
          }

          for (var i = startOffset; i < endOffset; i++) {
            if (me.siteDialog.buttons[i] != null) {
              me.siteDialog.buttons[i].appendTo("button-container");
            }
          }

          // Update the Paginator's state, confirming change
          me.siteDialog.pager.setState(state);
        };
      this.siteDialog.pager.subscribe('changeRequest', handlePagination, me);
      this.siteDialog.pager.render();

      //Activation bouton + cache de l'icône de chargement

      this.widgets.siteButton.set("disabled", false);

      this.widgets.siteButton.set("label", this._msg("button.choose"));
    },

    cleanSiteDialog: function ConsoleAudit_cleanSiteDialog(scope) {
      for (var i = 0, ii = scope.buttons.length; i < ii; i++) {
        if (scope.buttons[i].get("checked")) {
          scope.buttons[i].set("checked", false);
        }
      }
      this.selectedSites = [];
      scope._aButtons[1].blur();
    },
    /**
     * @method onShowSites
     *
     */
    onShowSites: function ConsoleAudit_onShowSites() {
      this.siteDialog.show();
    },

    onExport: function ConsoleAudit_onExport() {
      if (this.lastRequest.params) {
        var url = Alfresco.constants.PROXY_URI + "share-stats/export-audits" + this.lastRequest.params; //?json=" + escape(YAHOO.lang.JSON.stringify(this.lastRequest.data));//JSON.stringify
        window.open(url);
      }
    },

    /**
     * @method
     * @param
     * @return
     */
    onSearch: function ConsoleAudit_onSearch() {
      //Récupération des variables de l'UI
      var action = this.convertMenuValue(this.widgets.actionCriteriaButton.value),
        module = this.convertMenuValue(this.widgets.moduleCriteriaButton.value),
        dateFilter = this.currentDateFilter,
        site = null,
        sites = "",
        type = "",
        tsArray = [];

      for (var i = 0, ii = this.selectedSites.length; i < ii; i++) {
        sites += this.selectedSites[i].shortName;
        if (i < ii - 1) {
          sites += ",";
        }
      }
      // Crétion du tableau d'intervalle de dates
      if (dateFilter) {
        tsArray = this.buildTimeStampArray();
      }

      //Avertissement concernant le rendu
      if (module == null && action == null) {
        Dom.get("request-information").innerHTML = this._msg("warning.results");
      } else {
        Dom.get("request-information").innerHTML = "";
      }

      //Recupération du type de requête
      type = action;

      //Mise à jour du paramètre site si un seul site est choisi.
      site = (this.selectedSites.length == 1) ? this.selectedSites[0].shortName : null;

      //Test sur les valeurs de dates
      if (this.selectedSites.length < 1) {
        var me = this;
        Alfresco.util.PopupManager.displayPrompt({
          text: this._msg("label.popup.error.site"),
          title: this._msg("label.popup.error.title"),
          buttons: [{
            "text": me._msg("button.ok"),
            handler: function () {
              this.hide();
              me.siteDialog.show();
            },
            isDefault: true
          }]
        });
      } else {
        // Création des paramètres et exécution de la requête
        this.lastRequest.params = this.buildParams(action, module, site, sites, tsArray.toString(), type);

        var url = Alfresco.constants.PROXY_URI + "share-stats/select-audits" + this.lastRequest.params;
        Alfresco.util.Ajax.jsonGet({
          url: url,
          successCallback: {
            fn: this.displayGraph,
            scope: this
          },
          failureMessage: this._msg("Query error"),
          execScripts: true
        });
      }
      // Problème de focus avec le bouton et flash
      this.widgets.searchButton.blur();
    },

    /**
     * @method displayGraph Affiche le requête suite à une requête Ajax
     * @param response Réponse de la requête
     */
    displayGraph: function ConsoleAudit_displayGraph(response) {
      var swf = Dom.get(this.id + "-chart"),
        chartTag = swf.tagName.toLowerCase();

      if (response.json) {
        this.widgets.exportButton.set("disabled", false);
        response.json.currentFilter = this.currentDateFilter;
        // console.log(getFlashData(escape(YAHOO.lang.JSON.stringify(response.json))));
        if (this.countGraphItems(response.json) < 100) {
          if (chartTag == "embed" || chartTag == "object") {
            swf.load(getFlashData(escape(YAHOO.lang.JSON.stringify(response.json))));
          } else {
            //Création variables et attribut - GetFlashData défini dans get_data.js
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
            swfobject.embedSWF(this.pathToSwf, this.id + "-chart", "90%", "450", "9.0.0", "expressInstall.swf", flashvars, params, attributes);
          }
        } else {
          this.removeGraph();
          //Avertissement concernant le rendu
          Dom.get("request-information").innerHTML = this._msg("warning.no-graph");
        }
      } else {
        //On remove le SWF courant.
        this.removeGraph();
        Dom.get(this.id + "-chart").innerHTML = this._msg("message.no_results");
        this.widgets.exportButton.set("disabled", true);
      }
      this.widgets.searchButton.blur();
    },

    /**
     * @method removeGraph
     * @return boolean
     */
    removeGraph: function ConsoleAudit_removeGraph() {
      var swf = Dom.get(this.id + "-chart"),
        chartTag = swf.tagName.toLowerCase(),
        res = false;

      if (chartTag == "embed" || chartTag == "object") {
        swfobject.removeSWF(this.id + "-chart");
        //Le conteneur étant détruit, il faut le recréer ...
        var newChartDiv = new YAHOO.util.Element(document.createElement("div"));
        newChartDiv.set("id", this.id + "-chart");
        newChartDiv.appendTo(this.id + "-chart-container");
        res = true;
      }

      return res;
    },

    /**
     *
     * @method countGraphItems
     * @return integer
     */
    countGraphItems: function ConsoleAudit_countGraphItems(json) {
      var count = 0;
      if (json.slicedDates) {
        var maxItems = 0,
          item, i;
        for (i in json.items) {
          item = json.items[i];
          maxItems = (item.totalResults > maxItems) ? item.totalResults : maxItems;
        }
        count = maxItems * json.totalResults;
      } else {
        count = json.totalResults;
      }

      return count;
    },
    /**
     * @method convertDate
     * @param d Date au format jj/mm/aaaa
     * @return integer Timestamp unix de la date
     */
    convertDate: function ConsoleAudit_convertDate(d) {
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
    convertTimeStamp: function ConsoleAudit_convertTimeStamp(ts, exclude) {
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
    convertMenuValue: function ConsoleAudit_convertMenuValue(val) {
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

       * @return string params argument à passer à la requête
       */
    buildParams: function ConsoleAudit_buildParams(action, module, site, sites, dates, type) {
      var params = "?type=" + type;

      if (dates !== null && dates != "") {
        params += "&dates=" + dates;
      }
      // if(action !== null){
      // params += "&from="+action;
      // }
      if (module !== null) {
        params += "&module=" + module;
      }
      if (site !== null) {
        params += "&site=" + site;
      } else if (sites !== null && sites !== "") {
        params += "&sites=" + sites;
      }

      return params;
    },

    /**
     * @method buildTimeStampArray Construit des intervalles de dates
     * @param nbInterval Nombre d'intervalle de découpage
     * @param pTo Date de fin du découpage
     * @param type Type de découpage (Mois/Jour/Semaine)
     *
     * @return array Tableau contenant les différents intervalles de dates
     */
    buildTimeStampArray: function ConsoleAudit_buildTimeStampArray(nbInterval) {
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
        next.setDate(next.getDate() + 3);

        // Date d'arrêt
        to.setDate(1);
        to.setMonth(to.getMonth() + 1);

        hasNext = (to.getTime() > next.getTime());
        while (hasNext) {
          tsArray.push(next.getTime());
          next.setDate(next.getDate() + 3);
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

    onChangeDateFilter: function ConsoleAudit_OnChangeDateFilter(e, args) {
      Dom.removeClass("by-" + this.currentDateFilter, "selected");
      Dom.addClass(e.currentTarget, "selected");
      this.currentDateFilter = args.filter;
      this.onSearch();
    },


    // Args ?
    onChangeDateInterval: function ConsoleAudit_OnChangeDateInterval(e, args) {
      var coef = (args.interval == "next") ? 1 : -1,
        currentDate = new Date(),
        dateFilter = this.currentDateFilter,
        newDate = new Date(this.endDatesArray[dateFilter]),
        canNext = true;

      switch (dateFilter) {
      case "days":
        newDate.setDate(this.endDatesArray[dateFilter].getDate() + (1 * coef));
        canNext = (newDate > currentDate) ? false : true;
        break;
      case "weeks":
        newDate.setDate(this.endDatesArray[dateFilter].getDate() + (7 * coef));
        canNext = (newDate.getDay(0) > currentDate) ? false : true;
        break;
      case "months":
        newDate.setMonth(this.endDatesArray[dateFilter].getMonth() + (1 * coef));
        if (newDate.getFullYear() > currentDate.getFullYear() || (newDate.getMonth() >= currentDate.getMonth() && newDate.getFullYear() == currentDate.getFullYear())) {
          canNext = false;
        }
        break;
      case "years":
        newDate.setFullYear(this.endDatesArray[dateFilter].getFullYear() + (1 * coef));
        canNext = (newDate.getFullYear() > currentDate.getFullYear()) ? false : true;
        break;
      }

      // On enregistre la nouvelle date
      if (!coef || (coef && canNext)) {
        this.endDatesArray[dateFilter] = newDate;
      }
      this.onSearch();
    },
    onSearchClick: function ConsoleAudit_onSearchClick() {
      this.refreshUIState({
        "Time": new Date().getTime()
      });
    },

    //Traduction des messages
    _msg: function ConsoleAudit__msg(messageId) {
      return Alfresco.util.message.call(this, messageId, "Alfresco.ConsoleAudit", Array.prototype.slice.call(arguments).slice(1));
    },
  });
})();