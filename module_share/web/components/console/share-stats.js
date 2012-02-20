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
 */ (function () {
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
        parent.widgets.resetButton = Alfresco.util.createYUIButton(parent, "reset-dates-button", parent.onResetDates);

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

        parent.widgets.dateCriteriaButton = new YAHOO.widget.Button("date-criteria", {
          type: "split",
          menu: "date-criteria-select"
        });

        parent.widgets.siteCriteriaButton = new YAHOO.widget.Button("site-criteria", {
          type: "split",
          menu: "site-criteria-select",
          lazyloadmenu: false
        });

				parent.widgets.resetButton.addClass("share-stats-button");
				parent.widgets.siteButton.addClass("share-stats-button");
				parent.widgets.exportButton.addClass("share-stats-button");
				parent.widgets.searchButton.addClass("share-stats-button");
				parent.widgets.moduleCriteriaButton.addClass("share-stats-button");
				parent.widgets.actionCriteriaButton.addClass("share-stats-button");
				parent.widgets.dateCriteriaButton.addClass("share-stats-button");
				parent.widgets.siteCriteriaButton.addClass("share-stats-button");

        parent.widgets.startCalendar = new YAHOO.widget.Calendar("calendar-date-from", "calendar-date-from", {
          mindate: "1/1/2011",
          title: parent._msg("title.fromDate"),
          close: true
        });

        parent.widgets.endCalendar = new YAHOO.widget.Calendar("calendar-date-to", "calendar-date-to", {
          mindate: "1/1/2011",
          title: parent._msg("title.toDate"),
          close: true
        });

        parent.localizeCalendar(parent.widgets.startCalendar);
        parent.localizeCalendar(parent.widgets.endCalendar);
        //Handler des click-icônes
        var onIconFromClick = function (e) {
            var endCalendarVisible = Dom.getStyle(parent.widgets.endCalendar.id, 'display'),
              startCalendarVisible = Dom.getStyle(parent.widgets.startCalendar.id, 'display');
            if (startCalendarVisible == "none") {
              Dom.setStyle(parent.widgets.startCalendar.id, 'top', parseInt(e.layerY - 150, 10) + "px");
              Dom.setStyle(parent.widgets.startCalendar.id, 'left', parseInt(e.layerX, 10) + "px");
              parent.widgets.startCalendar.show();
            } else {
              parent.widgets.startCalendar.hide();
            }
            //Au cas où le second calendrier est toujours ouvert
            if (endCalendarVisible == "block") {
              parent.widgets.endCalendar.hide();
            }
          };

        var onIconToClick = function (e) {
            var endCalendarVisible = Dom.getStyle(parent.widgets.endCalendar.id, 'display'),
              startCalendarVisible = Dom.getStyle(parent.widgets.startCalendar.id, 'display');
            if (endCalendarVisible == "none") {
              Dom.setStyle(parent.widgets.endCalendar.id, 'top', parseInt(e.layerY - 150, 10) + "px");
              Dom.setStyle(parent.widgets.endCalendar.id, 'left', parseInt(e.layerX, 10) + "px");
              parent.widgets.endCalendar.show();
            } else {
              parent.widgets.endCalendar.hide();
            }
            //Au cas où le second calendrier est toujours ouvert
            if (startCalendarVisible == "block") {
              parent.widgets.startCalendar.hide();
            }
          };
        Event.addListener("icon-from", "click", onIconFromClick);
        Event.addListener("icon-to", "click", onIconToClick);

        //Handler de la selection des dates
        var onStartCalendarSelect = function (type, args, obj) {
            var dates = args[0],
              date = dates[0],
              year = date[0],
              month = date[1],
              day = date[2];
            Dom.get("input-date-from").value = day + '/' + month + '/' + year;
            this.hide();
          };

        var onEndCalendarSelect = function (type, args, obj) {
            var dates = args[0],
              date = dates[0],
              year = date[0],
              month = date[1],
              day = date[2];
            Dom.get("input-date-to").value = day + '/' + month + '/' + year;
            this.hide();
          };
        parent.widgets.startCalendar.selectEvent.subscribe(onStartCalendarSelect, parent.widgets.startCalendar, true);
        parent.widgets.endCalendar.selectEvent.subscribe(onEndCalendarSelect, parent.widgets.endCalendar, true);

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
            value = p_aArgs[1].value,
            disabled = p_aArgs[1].cfg.getProperty("disabled"),
            displayPopup = false;

          //On vérifie que l'option du menu ne soit pas disabled pour ne pas déclencher d'évenements.
          if (!disabled) {
            me.widgets.moduleCriteriaButton.value = p_aArgs[1].value;
            me.widgets.moduleCriteriaButton.set("label", sText);
            //Vérification que les choix concordent bien
            var menuItems = me.widgets.actionCriteriaButton.getMenu().getItems(),
              menuItem = null;
            if (value != "" && value != "document") {
              for (var i = 0, ii = menuItems.length; i < ii; i++) {
                menuItem = menuItems[i];

                if (menuItem.value == "file") {
                  if (!menuItem.cfg.getProperty("disabled")) {
                    menuItem.cfg.setProperty("disabled", true);
                    displayPopup = true;
                  }
                  menuItem.unsubscribe("click", onModulesMenuItemClick);
                  if (me.widgets.actionCriteriaButton.value == "file") {
                    me.widgets.actionCriteriaButton.value = "";
                    me.widgets.actionCriteriaButton.set("label", me._msg("label.menu.action") + me._msg("label.menu.none"));
                  }
                } else if (menuItem.value == "comment") {
                  if (value != "wiki" && value != "blog" && value != "discussions" && value != "links") {
                    if (!menuItem.cfg.getProperty("disabled")) {
                      menuItem.cfg.setProperty("disabled", true);
                      displayPopup = true;
                    }
                    if (me.widgets.actionCriteriaButton.value == "comment") {
                      me.widgets.actionCriteriaButton.value = "";
                      me.widgets.actionCriteriaButton.set("label", me._msg("label.menu.action") + me._msg("label.menu.none"));
                    }
                  }
                } else if (menuItem.value != "") {
                  // On ignore le menu "tous"
                  menuItem.cfg.setProperty("disabled", false);
                }
              }
            } else {
              for (var i = 0, ii = menuItems.length; i < ii; i++) {
                menuItem = menuItems[i];
                if (menuItem.cfg.getProperty("disabled") && menuItem.value != "") {
                  menuItem.cfg.setProperty("disabled", false);
                }
              }
            }
            me.widgets.moduleCriteriaButton.getMenu().hide();
          } else {
            me.widgets.moduleCriteriaButton.getMenu().show();
          }
          if (displayPopup) {
            Alfresco.util.PopupManager.displayMessage({
              title: me._msg("label.popup.warning.title"),
              text: me._msg("label.popup.warning.modules-click")
            });
          }
        };
      this.widgets.moduleCriteriaButton.getMenu().subscribe("click", onModulesMenuItemClick);

      // Comportement du menu de filtre par Actions
      var onActionsMenuItemClick = function (p_sType, p_aArgs, p_oItem) {
          var sText = p_aArgs[1].cfg.getProperty("text"),
            value = p_aArgs[1].value,
            disabled = p_aArgs[1].cfg.getProperty("disabled"),
            displayPopup = false;

          if (!disabled) {
            me.widgets.actionCriteriaButton.value = p_aArgs[1].value;
            me.widgets.actionCriteriaButton.set("label", sText);

            //Vérification que les choix concordent bien
            var menuItems = me.widgets.moduleCriteriaButton.getMenu().getItems(),
              menuItem = null;
            if (value == "comment" || value == "file") {
              moduleValue = me.widgets.moduleCriteriaButton.value;
              for (var i = 0, ii = menuItems.length; i < ii; i++) {
                menuItem = menuItems[i];
                if (menuItem.value != "" && ((value == "file" && menuItem.value != "document") || (value == "comment" && menuItem.value != "document" && menuItem.value != "blog" && menuItem.value != "wiki" && menuItem.value != "discussions" && menuItem.value != "links"))) {
                  if (!menuItem.cfg.getProperty("disabled")) {
                    menuItem.cfg.setProperty("disabled", true);
                    displayPopup = true;
                  }
                  //Le module sélectionné ne convient pas
                  if ((value == "file" && moduleValue != "document") || (value == "comment" && moduleValue != "document" && moduleValue != "blog" && moduleValue != "wiki" && moduleValue != "discussions" && moduleValue != "links")) {
                    me.widgets.moduleCriteriaButton.value = "";
                    me.widgets.moduleCriteriaButton.set("label", me._msg("label.menu.module") + me._msg("label.menu.all"));
                  }
                } else {
                  if (menuItem.cfg.getProperty("disabled")) {
                    menuItem.cfg.setProperty("disabled", false);
                  }
                }
              }
            } else {
              for (var i = 0, ii = menuItems.length; i < ii; i++) {
                menuItem = menuItems[i];
                if (menuItem.cfg.getProperty("disabled")) {
                  menuItem.cfg.setProperty("disabled", false);
                }
              }
            }
            me.widgets.actionCriteriaButton.getMenu().hide();
          } else {
            me.widgets.actionCriteriaButton.getMenu().show();
          }

          if (displayPopup) {
            Alfresco.util.PopupManager.displayMessage({
              title: me._msg("label.popup.warning.title"),
              text: me._msg("label.popup.warning.actions-click")
            });
          }
        };
      this.widgets.actionCriteriaButton.getMenu().subscribe("click", onActionsMenuItemClick);

      //Comportement du menu "Dates"
      var onDateMenuItemClick = function (p_sType, p_aArgs, p_oItem) {
          var sText = p_aArgs[1].cfg.getProperty("text"),
            value = p_aArgs[1].value;

          me.widgets.dateCriteriaButton.value = value;
          me.widgets.dateCriteriaButton.set("label", sText);
        };
      this.widgets.dateCriteriaButton.getMenu().subscribe("click", onDateMenuItemClick);

      //Comportement du menu "Sites"
      var onSiteMenuItemClick = function (p_sType, p_aArgs, p_oItem) {
          var sText = p_aArgs[1].cfg.getProperty("text"),
            siteValue = p_aArgs[1].value;
          disabled = p_aArgs[1].cfg.getProperty("disabled");

          me.widgets.siteCriteriaButton.set("label", sText);
          me.widgets.siteCriteriaButton.value = siteValue;
          //Vérification que les choix concordent bien
          var menuItems = me.widgets.actionCriteriaButton.getMenu().getItems();

          //Fonction d'affichage la liste des sites
          var displaySiteDialog = function (selectedSitesCount) {
              if (!me.widgets.siteCriteriaButton.get("disabled") && me.selectedSites.length <= selectedSitesCount) {
                me.siteDialog.show();
              }
            }

          if (siteValue === "/compare") {
            //On grise la sélection "tous" des actions
            if (!menuItems[0].cfg.getProperty("disabled")) {
              menuItems[0].cfg.setProperty("disabled", true);
            }

            // On dégrise le bouton de sélection des sites
            if (me.widgets.siteButton.get("disabled")) {
              me.widgets.siteButton.set("disabled", false);
            }
            me.widgets.siteButton.set("label", me._msg("button.choose"));

            if (!me.widgets.actionCriteriaButton.value) {
              me.widgets.actionCriteriaButton.value = "views";
              me.widgets.actionCriteriaButton.set("label", me._msg("label.menu.module") + me._msg("label.views"));

              //Pop-up de warning
              Alfresco.util.PopupManager.displayPrompt({
                text: me._msg("label.popup.warning.filter"),
                title: me._msg("label.popup.warning.title"),
                buttons: [{
                  "text": me._msg("button.ok"),
                  handler: function () {
                    this.hide();
                    displaySiteDialog(2);
                  },
                  isDefault: true
                }]
              });
            } else {
              displaySiteDialog(2);
            }

          } else {
            // Sélection de tous les sites
            if (siteValue === "all") {
              // Griser le bouton choisir
              me.widgets.siteButton.set("disabled", true);
              if (me.selectedSites != []) {
                me.cleanSiteDialog(me.siteDialog);
                Dom.setStyle("choosen-sites-container", 'display', 'none');
              }
            } else if (siteValue === "one") {
              // Dégriser
              me.widgets.siteButton.set("label", me._msg("button.choose.one"));
              me.widgets.siteButton.set("disabled", false);

              //Affichage de la popup
              displaySiteDialog(0);
            }
            if (menuItems[0].cfg.getProperty("disabled")) {
              menuItems[0].cfg.setProperty("disabled", false);
            }
          }
        };
      this.widgets.siteCriteriaButton.getMenu().subscribe("click", onSiteMenuItemClick);

      this.widgets.startCalendar.render();
      this.widgets.endCalendar.render();
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
      if (this.widgets.siteCriteriaButton.value && this.widgets.siteCriteriaButton.value !== "all") {
        this.widgets.siteButton.set("disabled", false);
      }
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


		onResetDates: function ConsoleAudit_onResetDates(){
			Dom.get("input-date-from").value = "";
			Dom.get("input-date-to").value = "";
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
      var from = this.convertDate(Dom.get("input-date-from").value),
        to = this.convertDate(Dom.get("input-date-to").value),
        action = this.convertMenuValue(this.widgets.actionCriteriaButton.value),
        module = this.convertMenuValue(this.widgets.moduleCriteriaButton.value),
        dates = this.convertMenuValue(this.widgets.dateCriteriaButton.value),
        siteValue = this.convertMenuValue(this.widgets.siteCriteriaButton.value),
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
      if (dates) {
        tsArray = this.buildTimeStampArray(from, to, dates);
        from = tsArray[0];
        to = tsArray[tsArray.length - 1];
        Dom.get("input-date-from").value = this.convertTimeStamp(from, false);
        Dom.get("input-date-to").value = this.convertTimeStamp(to, true);
        //Si seulement un intervalle, on supprime le tableau et on met à jour les dates from/to
        // -> Affichage camembert
        if (tsArray.length == 2) {
          tsArray = [];
          dates = null;
        }
      }

      //Avertissement concernant le rendu
      if (module == null && action == null) {
        Dom.get("request-information").innerHTML = this._msg("warning.results");
      } else {
        Dom.get("request-information").innerHTML = "";
      }

      //Recupération du type de requête
      type = this.getRequestType(action, module, siteValue, dates);

      //Mise à jour du paramètre site si un seul site est choisi.
      site = (this.selectedSites.length == 1) ? this.selectedSites[0].shortName : null;

      //Test sur les valeurs de dates
      if (to > 0 && from > to) {
        Alfresco.util.PopupManager.displayPrompt({
          text: this._msg("label.popup.error.date.greater"),
          title: this._msg("label.popup.error.title")
        });
      } else if (this.selectedSites.length < 2 && siteValue == "/compare" || this.selectedSites.length < 1 && siteValue == "one") {
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
        this.lastRequest.params = this.buildParams(from, to, action, module, site, sites, tsArray.toString(), type);

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
     * @method getRequestType Construit le paramètre type à partir différentes sélection du menu
     * @param action Valeur de l'action selectionnée
     * @param module Valeur du module selectionné
     * @param dates Valeur de la recherche par date
     *
     * @return string Type de requête à effectuer
     *
     */
    getRequestType: function ConsoleAudit_getRequestType(action, module, site, dates) {
      var type = "module",
        date = dates ? dates : "";

      switch (action) {
      case "views":
        if (site == "/compare") {
          type = "sitesview" + date;
        } else {
          type = "moduleviews" + date;
        }
        break;
      case "comment":
        if (site == "/compare") {
          type = "sitescomment" + date;
        } else {
          type = "comment" + date
        }
        break;
      case "file":
        if (site == "/compare") {
          type = "sitesfile" + date;
        } else {
          type = "file" + date;
        }
        break;
      case null:
        type = "action" + date;
        break;
      default:
        type = "module" + date;
        break;
      }

      return type;
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
    buildParams: function ConsoleAudit_buildParams(from, to, action, module, site, sites, dates, type) {
      var params = "?type=" + type;

      if (dates !== null && dates != "") {
        params += "&dates=" + dates;
      } else {
        if (from !== null && from > 0) {
          params += "&from=" + from;
        }

        if (to !== null && to > 0) {
          params += "&to=" + to;
        }
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
     * @param pFrom Date de départ du découpage
     * @param pTo Date de fin du découpage
     * @param type Type de découpage (Mois/Jour/Semaine)
     *
     * @return array Tableau contenant les différents intervalles de dates
     */
    buildTimeStampArray: function ConsoleAudit_buildTimeStampArray(pFrom, pTo, type) {
      var tsArray = [],
        from = null,
        to = null,
        currentDay = null,
        next = null,
        hasNext = null,
        res = "";

      //Utilisation de la date courante dans si les dates sont mal saisies
      // -> Audit sur mois/semaine/jour courant
      if (pFrom == 0 && pTo == 0) {
        from = new Date();
        from.setMinutes(0);
        from.setHours(0);
        from.setMinutes(0);
        from.setSeconds(0);
        from.setMilliseconds(0);
        to = new Date();
        to.setMinutes(0);
        to.setHours(0);
        to.setMinutes(0);
        to.setSeconds(0);
        to.setMilliseconds(0);
      } else if (pFrom == 0) {
        from = new Date(pTo);
        to = new Date(pTo);
      } else if (pTo == 0) {
        from = new Date(pFrom);
        to = new Date(pFrom);
      } else {
        from = new Date(pFrom);
        to = new Date(pTo);
      }

      // Créé les intervalles allant du mois de départ au mois d'arrivée INCLUS
      if (type == "_by_month") {
        tsArray.push(from.setDate(1));
        next = new Date(from);
        next.setDate(1);
        next.setMonth(next.getMonth() + 1);

        hasNext = (to.getTime() >= next.getTime());
        while (hasNext) {
          tsArray.push(next.getTime());
          next.setMonth(next.getMonth() + 1);
          hasNext = (to.getTime() >= next.getTime());
        }
        tsArray.push(next.getTime());
      }
      // Selectionne par semaine suivant from et to.
      // Les semaines de "from" et "to" sont INCLUSES
      else if (type == "_by_week") {
        //On utilise la date de départ pour récupérer tous les jours de la semaine
        next = null, currentDay = from.getDay(), hasNext = false;
        //Début de semaine
        from.setDate(from.getDate() - (currentDay - 1));
        next = new Date(from);
        tsArray.push(from.getTime());

        //Semaine suivante, on test au cas où on dépasse.
        next.setDate(from.getDate() + 7);
        hasNext = (to.getTime() >= next.getTime());
        while (hasNext) {
          tsArray.push(next.getTime());
          next.setDate(next.getDate() + 7);
          hasNext = (to.getTime() >= next.getTime());
        }
        tsArray.push(next.getTime());
      }
      // Créé les intervalles allant du jour de départ au jour d'arrivée INCLUS
      else if (type == "_by_day") {
        //On ajoute la date de départ
        tsArray.push(from.getTime());

        //On ajoute 1 jour à la date de fin, pour inclure le dernier jour selectionné.
        to.setDate(to.getDate() + 1);

        //On récupère le jour suivant
        next = new Date(from);
        next.setDate(next.getDate() + 1);

        //On vérifie qu'il ne dépasse pas la date de fin, on boucle
        hasNext = (to > next);
        while (hasNext) {
          tsArray.push(next.getTime());
          next.setDate(next.getDate() + 1);
          hasNext = (to > next);
        }
        tsArray.push(to.getTime());
      }

      return tsArray;
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

    /**
     * @method localizeCalendar
     * @param cal Calendrier Yahoo à passer en fr
     *
     *
     */
    localizeCalendar: function ConsoleAudit_localizeCalendar(cal) {
      cal.cfg.setProperty("DATE_FIELD_DELIMITER", "/");

      cal.cfg.setProperty("MDY_DAY_POSITION", 1);
      cal.cfg.setProperty("MDY_MONTH_POSITION", 2);
      cal.cfg.setProperty("MDY_YEAR_POSITION", 3);

      cal.cfg.setProperty("MD_DAY_POSITION", 1);
      cal.cfg.setProperty("MD_MONTH_POSITION", 2);

      // Date labels for German locale
      cal.cfg.setProperty("MONTHS_SHORT", ["Jan", "Fev", "Mars", "Avr", "Mai", "Juin", "Juil", "Aout", "Sep", "Oct", "Nov", "Dec"]);
      cal.cfg.setProperty("MONTHS_LONG", ["Janvier", "F\u00e9vrier", "Mars", "Avril", "Mai", "Juin", "Juillet", "Ao\u00fbt", "Septembre", "Octobre", "Novembre", "D\u00e9cembre"]);
      cal.cfg.setProperty("WEEKDAYS_1CHAR", ["D", "L", "M", "M", "J", "V", "S"]);
      cal.cfg.setProperty("WEEKDAYS_SHORT", ["Di", "Lu", "Ma", "Me", "Je", "Ve", "Sa"]);
      cal.cfg.setProperty("WEEKDAYS_MEDIUM", ["Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"]);
      cal.cfg.setProperty("WEEKDAYS_LONG", ["Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"]);

      // Start the week on a Monday (Sunday == 0)
      cal.cfg.setProperty("START_WEEKDAY", 1);
    }
  });
})();