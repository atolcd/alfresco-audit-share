// AtolStatistics namespace
if (typeof AtolStatistics == undefined || !AtolStatistics) { var AtolStatistics = {}; }

/**
 * UserConnections tool component.
 *
 * @namespace AtolStatistics
 * @class AtolStatistics.UserConnections
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
   * UserConnections constructor.
   *
   * @param {String} htmlId The HTML id üof the parent element
   * @return {AtolStatistics.UserConnections} The new UserConnections instance
   * @constructor
   */
  AtolStatistics.UserConnections = function UserConnections_constructor(htmlId) {
    AtolStatistics.UserConnections.superclass.constructor.call(this, "AtolStatistics.UserConnections", htmlId, ["button", "container", "json"]);

    // Surcharge de la classe Date. Récupère la semaine courante
    Date.prototype.getWeek = function() {
     var onejan = new Date(this.getFullYear(),0,1);
     return Math.ceil((((this - onejan) / 86400000) + onejan.getDay()+1)/7);
    };

    return this;
  };

  YAHOO.extend(AtolStatistics.UserConnections, Alfresco.component.Base, {

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
     * @attribute recentlyConnectedDelay
     * Durée (en minutes) depuis laquelle on retrouve les utilisateurs récemments connectés
     */
    recentlyConnectedDelay: 30,

    /**
     * @attribute headers
     * Contient les éléments Dom des différents headers de la table sous le graphe
     */
    headers: [],

    /**
     * Fired by YUILoaderHelper when required component script files have
     * been loaded into the browser.
     *
     * @method onComponentsLoaded
     */
    onComponentsLoaded: function UserConnections_onComponentsLoaded() {
      Event.onContentReady(this.id, this.onReady, this, true);
    },

    /**
     * Fired by YUI when parent element is available for scripting.
     * Component initialisation, including instantiation of YUI widgets and event listener binding.
     *
     * @method onReady
     */
    onReady: function UserConnections_onReady() {
      this.setupCurrentDates();

      // Buttons - Check ?
      this.widgets.exportButton = Alfresco.util.createYUIButton(this, "export-button", this.onExport);

      this.widgets.exportButton.set("disabled", true);

      // el, sType, fn, obj, overrideContext
      Event.addListener("home", "click", this.onResetDates, null, this);
      Event.addListener("by-days", "click", this.onChangeDateFilter, { filter: "days" }, this);
      Event.addListener("by-weeks", "click", this.onChangeDateFilter, { filter: "weeks" }, this);
      Event.addListener("by-months", "click", this.onChangeDateFilter, { filter: "months" }, this);
      Event.addListener("by-years", "click", this.onChangeDateFilter, { filter: "years" }, this);

      Event.addListener("chart-prev", "click", this.onChangeDateInterval, { coef: -1 }, this);
      Event.addListener("chart-next", "click", this.onChangeDateInterval, { coef: 1 }, this);

      this.headers["users-connected"] = Dom.get(this.id + "-users-connected-header");
      this.headers["users-never-connected"] = Dom.get(this.id + "-users-never-connected-header");
      this.headers["users-recently-connected"] = Dom.get(this.id + "-users-recently-connected-header");

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
    createSiteMenu: function UserConnections_createSiteDialog(res) {
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
        container: "site-criteria-container"
      });

      //Maj des infos du bouttons
      this.widgets.siteButton.set("label", this.msg("label.menu.site.all"));
      this.widgets.siteButton.value = "";

      this.execute();
    },

    onExport: function UserConnections_onExport() {
      if (this.lastRequest.params) {
        var params = this.lastRequest.params;
        params += "&type=users";
        params += "&values=" + this.lastRequest.values.toString();
        params += "&interval=" + this.lastRequest.dateFilter;
        var url = Alfresco.constants.PROXY_URI + "share-stats/export-audits" + params; //?json=" + escape(YAHOO.lang.JSON.stringify(this.lastRequest.data));//JSON.stringify
        window.open(url);
      }
    },

    /**
     * @method prepareRecentlyConnectedUsersRequest
     */
    prepareRecentlyConnectedUsersRequest: function UserConnections_prepareRecentlyConnectedUsersRequest() {
      //Récupération des variables de l'UI
      var site = this.convertMenuValue(this.widgets.siteButton.value),
        currentDate = new Date(),
        params = "";

      params = "?type=users-recently-connected";
      params += "&to=" + currentDate.getTime();
      params += "&from=" + currentDate.setMinutes(currentDate.getMinutes() - this.recentlyConnectedDelay);
      if (site) {
        params += "&site=" + site;
      }

      this.executeUserRequest(params, "users-recently-connected");
    },

    /**
     * @method prepareUserRequest
     */
    prepareUserRequest: function UserConnections_prepareUserRequest(type) {
      //Récupération des variables de l'UI
      var dateFilter = this.currentDateFilter,
        site = this.convertMenuValue(this.widgets.siteButton.value),
        params = "";

      // Crétion du tableau d'intervalle de dates
      tsArray = this.buildTimeStampArray();
      params = "?type=" + type;
      params += "&from=" + tsArray[0];
      params += "&to=" + tsArray[tsArray.length - 1];
      if (site) {
        params += "&site=" + site;
      }

      this.executeUserRequest(params, type);
    },

    /**
     * @method executeUserRequest
     */
    executeUserRequest: function UserConnections_executeUserRequest(params, type) {
      var displayUsers = function (response) {
          var users = response.json.items,
            el = Dom.get(this.id + "-" + type);

          if (users && users.length) {
            var context = Alfresco.constants.URL_PAGECONTEXT,
              url = "",
              html = "",
              user = "",
              i = 0,
              l = users.length;

            for (; i < l; i++) {
              user = users[i];
              html += '<li class="nav-user">';
              html += '<a class="theme-color-1" tabindex="0" target="_blank" href="' + context + 'user/' + user.username + '/profile">' + user.fullName + ' (' + user.username + ')</a>';
              html += '</li>';
            }

            html = "<ul>" + html + "</ul>";
            el.innerHTML = html;
          } else {
            el.innerHTML = this.msg("label.no-results." + type);
          }

          this.headers[type].innerHTML += " (" + users.length + ")";
        };

      var url = Alfresco.constants.PROXY_URI + "share-stats/select-users" + params;
      Alfresco.util.Ajax.jsonGet({
        url: url,
        successCallback: {
          fn: displayUsers,
          scope: this
        },
        failureMessage: this.msg("label.popup.query.error"),
        execScripts: true
      });
    },
    /**
     * @method
     * @param
     * @return
     */
    onSearch: function UserConnections_onSearch() {
      //Récupération des variables de l'UI
      var dateFilter = this.currentDateFilter,
        site = this.convertMenuValue(this.widgets.siteButton.value),
        tsString = "",
        params = "";

      // Crétion du tableau d'intervalle de dates
      if (dateFilter) {
        var tsArray = this.buildTimeStampArray();
        //Mise à jour des labels
        this.updateUsersLabels(tsArray);
        tsString = tsArray.toString();
      }

      // Création des paramètres et exécution de la requête
      params = "?type=users-count";
      params += "&dates=" + tsString;
      if (site) {
        params += "&site=" + site;
      }
      this.lastRequest.params = params;
      this.lastRequest.dateFilter = dateFilter;

      var url = Alfresco.constants.PROXY_URI + "share-stats/select-users" + this.lastRequest.params;
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
          type: "count",
          site: site,
          tsString: tsString,
          target: "chart",
          height: "450",
          width: "90%"
        }
      });

    },

    /**
     * @method displayGraph Affiche le requête suite à une requête Ajax
     * @param response Réponse de la requête
     */
    displayGraph: function UserConnections_displayGraph(response) {
      var additionalsParams, id, swf, chartTag;

      additionalsParams = response.config.additionalsParams;
      id = this.id + "-" + additionalsParams.target;
      swf = Dom.get(id);
      chartTag = swf.tagName.toLowerCase();

      if (response.json) {
        this.widgets.exportButton.set("disabled", false);
        response.json.currentFilter = this.currentDateFilter;
        response.json.additionalsParams = additionalsParams;
        this.lastRequest.values = response.json.values;

        if (chartTag == "embed" || chartTag == "object") {
          swf.load(getUserFlashData(escape(YAHOO.lang.JSON.stringify(response.json))));
        } else {
          //Création variables et attribut - GetFlashData défini dans get_data.js - id : Variables json pour ofc.
          var flashvars = {
            "get-data": "getUserFlashData",
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
    },

    /**
     * @method removeGraph
     * @return boolean
     */
    removeGraph: function UserConnections_removeGraph(id) {
      var swf = Dom.get(id),
        chartTag = swf.tagName.toLowerCase(),
        res = false;

      if (chartTag == "embed" || chartTag == "object") {
        swfobject.removeSWF(id);
        // Le conteneur étant détruit, il faut le recréer ...
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
    convertDate: function UserConnections_convertDate(d) {
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
    convertTimeStamp: function UserConnections_convertTimeStamp(ts, exclude) {
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
    convertMenuValue: function UserConnections_convertMenuValue(val) {
      var res = null;
      if (val !== undefined && val !== "") {
        res = val;
      }
      return res;
    },


    /**
     * @method buildParams Construit une chaîne de caractère pour passer les arguments en GET
     * @return string params argument à passer à la requête
     */
    buildParams: function UserConnections_buildParams() {
      var params = "";

      // TODO:

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
    buildTimeStampArray: function UserConnections_buildTimeStampArray(nbInterval) {
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

    updateUsersLabels: function UserConnections_updateUsersLabels(tsArray) {
      var connectedLabel = "",
        neverConnectedLabel = "";

      switch (this.currentDateFilter) {
      case "days":
        var date = new Date(tsArray[0]),
          today = new Date();

        if (date.getMonth() == today.getMonth() && date.getDate() == today.getDate() && date.getFullYear() == today.getFullYear()) {
          connectedLabel = this.msg("label.users.connected.today");
          neverConnectedLabel = this.msg("label.users.never-connected.today");
        } else {
          var day = this.msg("label.day." + date.getDay());
          day += " " + padzero(date.getDate()) + "/" + padzero(date.getMonth() + 1);
          connectedLabel = this.msg("label.users.connected." + this.currentDateFilter, day);
          neverConnectedLabel = this.msg("label.users.never-connected." + this.currentDateFilter, day);
        }
        break;
      case "weeks":
        var fromDate = new Date(tsArray[0]),
          lastDate = new Date(tsArray[tsArray.length - 2]),
          from, to;
        from = padzero(fromDate.getDate()) + "/" + padzero(fromDate.getMonth() + 1) + "/" + fromDate.getFullYear();
        to = padzero(lastDate.getDate()) + "/" + padzero(lastDate.getMonth() + 1) + "/" + lastDate.getFullYear();
        connectedLabel = this.msg("label.users.connected." + this.currentDateFilter, from, to);
        neverConnectedLabel = this.msg("label.users.never-connected." + this.currentDateFilter, from, to);
        break;
      case "months":
        var date = new Date(tsArray[0]),
          month;
        month = this.msg("label.month." + date.getMonth()) + " " + date.getFullYear();
        connectedLabel = this.msg("label.users.connected." + this.currentDateFilter, month);
        neverConnectedLabel = this.msg("label.users.never-connected." + this.currentDateFilter, month);
        break;
      case "years":
        var date = new Date(tsArray[0]);
        connectedLabel = this.msg("label.users.connected." + this.currentDateFilter, date.getFullYear());
        neverConnectedLabel = this.msg("label.users.never-connected." + this.currentDateFilter, date.getFullYear());
        break;
      };

      if (connectedLabel) {
        this.headers["users-connected"].innerHTML = connectedLabel;
      }
      if (neverConnectedLabel) {
        this.headers["users-never-connected"].innerHTML = neverConnectedLabel;
      }
      this.headers["users-recently-connected"].innerHTML = this.msg("label.users.recently-connected");
    },
    /**
     * @method onChangeDateFilter
     * @param e Event déclencheur
     * @param args Composant déclencheur
     * Gestionnaire click Jour / Semaine / Mois / Année
     */
    onChangeDateFilter: function UserConnections_OnChangeDateFilter(e, args) {
      if (e) Event.stopEvent(e);
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
    onChangeDateInterval: function UserConnections_OnChangeDateInterval(e, args) {
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

    onResetDates: function UserConnections_OnResetDates() {
      this.setupCurrentDates();
      this.execute();
    },

    execute: function UserConnections_execute() {
      this.prepareUserRequest("users-connected");
      this.prepareUserRequest("users-never-connected");
      this.prepareRecentlyConnectedUsersRequest();
      this.onSearch();
    },

    setupCurrentDates: function UserConnections_setupCurrentDates() {
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