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
(function()
{
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
   Alfresco.ConsoleAudit = function(htmlId)
   {
      this.name = "Alfresco.ConsoleAudit";
      Alfresco.ConsoleAudit.superclass.constructor.call(this, htmlId);

      /* Register this component */
      Alfresco.util.ComponentManager.register(this);

      /* Load YUI Components */
      Alfresco.util.YUILoaderHelper.require(["button", "container", "datasource", "datatable", "json", "history"], this.onComponentsLoaded, this);


      /* Define panel handlers */
      var parent = this;

      // NOTE: the panel registered first is considered the "default" view and is displayed first

      /* Search Panel Handler */
      AuditPanelHandler = function AuditPanelHandler_constructor()
      {
         AuditPanelHandler.superclass.constructor.call(this,"audit");
      };

      YAHOO.extend(AuditPanelHandler, Alfresco.ConsolePanelHandler,
      {

         /**
          * Called by the ConsolePanelHandler when this panel shall be loaded
          *
          * @method onLoad
          */
         onLoad: function onLoad()
         {
            // Buttons - Check ?
            parent.widgets.searchButton = Alfresco.util.createYUIButton(parent, "search-button", parent.onSearch);
            parent.widgets.exportButton = Alfresco.util.createYUIButton(parent, "export-button", parent.onExport);

            parent.widgets.exportButton.set("disabled",true);  
  
            parent.widgets.moduleCriteriaButton = new YAHOO.widget.Button("module-criteria", {
              type: "menu",
	            menu: "module-criteria-select" });

            parent.widgets.actionCriteriaButton = new YAHOO.widget.Button("action-criteria", {
              type: "menu",
              menu: "action-criteria-select" });

            parent.widgets.dateCriteriaButton = new YAHOO.widget.Button("date-criteria", {
              type: "menu",
              menu: "date-criteria-select" });

            this.setSiteMenu();
             // = new YAHOO.widget.Button({ type: "menu", label: "One", name: "mymenubutton", menu: aMenuButton5Menu, container: this });
            //var currentDate = new Date();
            //var stringDate = (currentDate.getMonth()+1) + '/' + currentDate.getDate() + '/' + currentDate.getFullYear();
            parent.widgets.startCalendar = new YAHOO.widget.Calendar("calendar-date-from","calendar-date-from",{
              mindate:"1/1/2011",
              // selected: stringDate,
              title: parent._msg("title.fromDate"),
              close: true});

            parent.widgets.endCalendar = new YAHOO.widget.Calendar("calendar-date-to","calendar-date-to",{
              mindate:"1/1/2011",
              // selected: stringDate,
              title: parent._msg("title.toDate"),
              close: true});


            //Handler des click-icônes
            var onIconFromClick = function(e){
              var endCalendarVisible = Dom.getStyle(parent.widgets.endCalendar.id,'display');
              var startCalendarVisible = Dom.getStyle(parent.widgets.startCalendar.id,'display');
              if(startCalendarVisible=="none") {
                Dom.setStyle(parent.widgets.startCalendar.id, 'top', parseInt(e.clientY,10)+"px");
                Dom.setStyle(parent.widgets.startCalendar.id, 'left', parseInt(e.clientX,10)+"px");
                parent.widgets.startCalendar.show();
              }
              else {
                parent.widgets.startCalendar.hide();
              }
              //Au cas où le second calendrier est toujours ouvert
              if(endCalendarVisible == "block"){
                parent.widgets.endCalendar.hide();
              }
            };

            var onIconToClick = function(e){
              var endCalendarVisible = Dom.getStyle(parent.widgets.endCalendar.id,'display');
              var startCalendarVisible = Dom.getStyle(parent.widgets.startCalendar.id,'display');
              if(endCalendarVisible=="none") {
                Dom.setStyle(parent.widgets.endCalendar.id, 'top', parseInt(e.clientY,10)+"px");
                Dom.setStyle(parent.widgets.endCalendar.id, 'left', parseInt(e.clientX,10)+"px");
                parent.widgets.endCalendar.show();
              }
              else {
                parent.widgets.endCalendar.hide();
              }
              //Au cas où le second calendrier est toujours ouvert
              if(startCalendarVisible == "block"){
                parent.widgets.startCalendar.hide();
              }
            };
            Event.addListener("icon-from","click",onIconFromClick);
            Event.addListener("icon-to","click",onIconToClick);

            //Handler de la selection des dates
            var onStartCalendarSelect = function(type,args,obj) {
              var dates = args[0];
              var date = dates[0];
              var year = date[0], month = date[1], day = date[2];
              Dom.get("input-date-from").value = day + '/' + month + '/' +year;
              this.hide();
            };

            var onEndCalendarSelect = function(type,args,obj) {
              var dates = args[0];
              var date = dates[0];
              var year = date[0], month = date[1], day = date[2];
              Dom.get("input-date-to").value = day + '/' + month + '/' +year;
              this.hide();
            };
            parent.widgets.startCalendar.selectEvent.subscribe(onStartCalendarSelect, parent.widgets.startCalendar, true);
            parent.widgets.endCalendar.selectEvent.subscribe(onEndCalendarSelect, parent.widgets.endCalendar, true);


            //Check plus loin pour response Schema ?
            // DataTable and DataSource setup
            /*parent.widgets.dataSource = new YAHOO.util.DataSource(Alfresco.constants.PROXY_URI + "db/select",
            {
               responseType: YAHOO.util.DataSource.TYPE_JSON,
               responseSchema:
               {
                  resultsList: "items",
                  metaFields:
                  {
                     recordOffset: "startIndex",
                     totalRecords: "totalRecords"
                  }
               }
            });

            // Setup the main datatable
            this._setupDataTable();*/
        },

        setSiteMenu: function setSiteMenu(){
          Alfresco.util.Ajax.jsonGet(
          {
             url: Alfresco.constants.PROXY_URI + "apca/site/list-sites",
             successCallback:
             {
                fn: function(res){
                  var siteMenu=[];
                  siteMenu[0]={text:this._msg("label.menu.site") + this._msg("label.menu.all"), value:""};
                  //Valeur avec "/" pour être sur que ça ne soit pas déjà un site existant
                  siteMenu[1]={text:this._msg("label.menu.site") + this._msg("label.menu.amongSites"), value:"/all"};
                  for(var i=0, ii = res.json.length;i<ii;i++) {
                    siteMenu[i+2]={ text: this._msg("label.menu.site") + res.json[i].title, value: res.json[i].name};
                  }

                  this.widgets.siteCriteriaButton = new YAHOO.widget.Button("site-criteria", {
                    type: "menu",
                    menu: siteMenu});

                  var me = this;
                  var onSiteMenuItemClick = function (p_sType, p_aArgs, p_oItem) {
                    var sText = p_aArgs[1].cfg.getProperty("text");
                    me.widgets.siteCriteriaButton.set("label", sText);
                    me.widgets.siteCriteriaButton.value = p_aArgs[1].value;
                  };
                  this.widgets.siteCriteriaButton.getMenu().subscribe("click",onSiteMenuItemClick);
                },
                scope: parent
             },
             failureMessage: parent._msg("error.list-site"),
             execScripts: true
          });
        },

        /*onUpdate: function onUpdate(){
          var me = this;

          // Don't display any message
          parent.widgets.dataTable.set("MSG_EMPTY", parent._msg("message.searching"));

          // Empty results table
          parent.widgets.dataTable.deleteRows(0, parent.widgets.dataTable.getRecordSet().getLength());

          var successHandler = function ConsoleUsers__ps_successHandler(sRequest, oResponse, oPayload)
          {
            parent.widgets.dataTable.onDataReturnInitializeTable.call(parent.widgets.dataTable, sRequest, oResponse, oPayload);
          };

          var failureHandler = function ConsoleUsers__ps_failureHandler(sRequest, oResponse)
          {
            if (oResponse.status == 401)
            {
              // Our session has likely timed-out, so refresh to offer the login page
              window.location.reload();
            }
            else
            {
              try
              {
                var response = YAHOO.lang.JSON.parse(oResponse.responseText);
                parent.widgets.dataTable.set("MSG_ERROR", response.message);
                parent.widgets.dataTable.showTableMessage(response.message, YAHOO.widget.DataTable.CLASS_ERROR);

              }
              catch(e)
              {
                console.log(e);
              }
            }
          };

         // Send the query to the server
         parent.widgets.dataSource.sendRequest("?type=all",
         {
            success: successHandler,
            failure: failureHandler,
            scope: parent
         });
        },*/

       /**
        * Setup the YUI DataTable with custom renderers.
        *
        * @method _setupDataTable
        * @private
        */
        _setupDataTable: function _setupDataTable()
        {
          /**
           * DataTable Cell Renderers
           *
           * Each cell has a custom renderer defined as a custom function. See YUI documentation for details.
           * These MUST be inline in order to have access to the parent instance (via the "parent" variable).
           */

          /**
           * User id custom datacell formatter
           *
           * @method renderCellFullName
           */
          var renderCellUserId = function renderCellUserId(elCell, oRecord, oColumn, oData)
          {
             elCell.innerHTML=oRecord.getData("auditUserId");
          };

          /**
           * Module custom datacell formatter
           *
           * @method renderCellModule
           */
          var renderCellModule = function renderCellModule(elCell, oRecord, oColumn, oData)
          {
            elCell.innerHTML=oRecord.getData("auditAppName");
          };

          /**
           * Action custom datacell formatter
           *
           * @method renderCellUsage
           */
          var renderCellAction= function renderCellAction(elCell, oRecord, oColumn, oData)
          {
             elCell.innerHTML=oRecord.getData("auditActionName");
          };

          /**
           * Site custom datacell formatter
           *
           * @method renderCellUsage
           */
          var renderCellSite = function renderCellSite(elCell, oRecord, oColumn, oData)
          {
             elCell.innerHTML=oRecord.getData("auditSite");
          };

          /**
           * Site custom datacell formatter
           *
           * @method renderCellUsage
           */
          var renderCellObject = function renderCellObject(elCell, oRecord, oColumn, oData)
          {
             elCell.innerHTML=oRecord.getData("auditObject");
          };

          /**
           * Site custom datacell formatter
           *
           * @method renderCellUsage
           */
          var renderCellTime = function renderCellTime(elCell, oRecord, oColumn, oData)
          {
            /* Trim the date get by json */
            var nbspRegEx = new RegExp(String.fromCharCode(160),"g");
            var d = new Date(parseInt(oRecord.getData("auditTime").replace(nbspRegEx,''),10));

            elCell.innerHTML=d.toLocaleDateString();

          };

          /**
           * Usage custom datacell sorter
           */

          // DataTable column defintions
          var columnDefinitions =
          [
             { key: "auditUserId", label: parent._msg("label.auditUserId"), sortable: true, formatter: renderCellUserId },
             { key: "auditAppName", label: parent._msg("label.auditAppName"), sortable: true, formatter: renderCellModule },
             { key: "auditActionName", label: parent._msg("label.auditActionName"), sortable: true, formatter: renderCellAction },
             { key: "auditSite", label: parent._msg("label.auditSite"), sortable: true, formatter: renderCellSite },
             { key: "auditObject", label: parent._msg("label.auditObject"), sortable: true, formatter: renderCellObject},
             { key: "auditTime", label: parent._msg("label.auditTime"), sortable: true, formatter: renderCellTime }
          ];

          // DataTable definition
         parent.widgets.dataTable = new YAHOO.widget.DataTable(parent.id + "-datatable", columnDefinitions, parent.widgets.dataSource,
          {
             initialLoad: false,
             renderLoopSize: 32,
             sortedBy:
             {
                key: "auditUserId",
                dir: "asc"
             },
             MSG_EMPTY: parent._msg("message.empty")
          });
        }
      });

      new AuditPanelHandler();

      return this;
   };

   YAHOO.extend(Alfresco.ConsoleAudit, Alfresco.ConsoleTool,
   {
  
      /**
       * Cache-Résultat de la dernière requête exécutée
       * Utilisé pour l'export CSV
       */
      lastRequest: null,
      /**
       * Fired by YUILoaderHelper when required component script files have
       * been loaded into the browser.
       *
       * @method onComponentsLoaded
       */
      onComponentsLoaded: function ConsoleAudit_onComponentsLoaded()
      {
         Event.onContentReady(this.id, this.onReady, this, true);


      },

      /**
       * Fired by YUI when parent element is available for scripting.
       * Component initialisation, including instantiation of YUI widgets and event listener binding.
       *
       * @method onReady
       */
      onReady: function ConsoleAudit_onReady()
      {

         //TODO
         // Call super-class onReady() method
         Alfresco.ConsoleAudit.superclass.onReady.call(this);

         //Composants créé, on ajoute des listeners sur les menus.
         var me = this;
          var onModulesMenuItemClick = function (p_sType, p_aArgs, p_oItem) {

            me.widgets.moduleCriteriaButton.value = p_aArgs[1].value;
            var sText = p_aArgs[1].cfg.getProperty("text");
            me.widgets.moduleCriteriaButton.set("label", sText);
          };
          this.widgets.moduleCriteriaButton.getMenu().subscribe("click",onModulesMenuItemClick);

          var onActionsMenuItemClick = function (p_sType, p_aArgs, p_oItem) {
            me.widgets.actionCriteriaButton.value = p_aArgs[1].value;
            var sText = p_aArgs[1].cfg.getProperty("text");
            me.widgets.actionCriteriaButton.set("label", sText);
          };
          this.widgets.actionCriteriaButton.getMenu().subscribe("click",onActionsMenuItemClick);

          var onDateMenuItemClick = function (p_sType, p_aArgs, p_oItem) {
            me.widgets.dateCriteriaButton.value = p_aArgs[1].value;
            var sText = p_aArgs[1].cfg.getProperty("text");
            me.widgets.dateCriteriaButton.set("label", sText);
          };
          this.widgets.dateCriteriaButton.getMenu().subscribe("click",onDateMenuItemClick);

          //
          this.widgets.startCalendar.render();
          this.widgets.endCalendar.render();
          
          //this.initMultiSelect();
      },

      
      onExport: function ConsoleAudit_onExport()
      {
       /* if(this.lastRequest) {
          var csv = "";
          for(var i = 0, ii = this.lastRequest.totalResults; i < ii; i++){
            csv += this.lastRequest.items[i].count.toString() + ",";
            csv += this.lastRequest.items[i].target + "\n";
          }
          console.log(csv);
          
          var url = Alfresco.constants.PROXY_URI + "share-stats/export-audits?csv="+csv;//JSON.stringify(this.lastRequest);
          Alfresco.util.Ajax.request(
          {
             url: url,
             method: Alfresco.util.Ajax.GET,
             successCallback:
             {
                // fn: this.displayGraph,
                // scope: this
             },
             failureMessage: this._msg("Query error"),
             execScripts: true
          });
        }*/
      },
      
      /**
       * @method
       * @param
       * @return
       */
      onSearch: function ConsoleAudit_onSearch()
      {
        //Récupération des variables de l'UI
        var from = this.convertDate(Dom.get("input-date-from").value), to = this.convertDate(Dom.get("input-date-to").value),
          action = this.convertMenuValue(this.widgets.actionCriteriaButton.value),
          module = this.convertMenuValue(this.widgets.moduleCriteriaButton.value),
          dates = this.convertMenuValue(this.widgets.dateCriteriaButton.value),
          site = this.convertMenuValue(this.widgets.siteCriteriaButton.value),
          tsArray = [];

        // Crétion du tableau d'intervalle de dates
        if(dates) {
          tsArray = this.buildTimeStampArray(from,to,dates);
          from = tsArray[0];
          to = tsArray[tsArray.length-1];
          Dom.get("input-date-from").value = this.convertTimeStamp(from,false);
          Dom.get("input-date-to").value = this.convertTimeStamp(to,true);
          //Si seulement un intervalle, on supprime le tableau et on met à jour les dates from/to
          // -> Affichage camembert
          if(tsArray.length==2) {
            tsArray=[];
            dates=null;
          }
        }

        //Recupération du type de requête
        var type = this.getRequestType(action,module,site,dates);
        
        //Mise à jour du paramètre site si "/all". La requête ne doit pas filtrer de site
        site = (site == "/all") ? null : site;
        
        //Test sur les valeurs de dates
        if(to > 0 && from > to){
          alert(this._msg("error.date.greater"));
        }
        else {
          // Création des paramètres et exécution de la requête
          var params = this.buildParams(from,to,action,module,site,tsArray.toString(),type);

          var url = Alfresco.constants.PROXY_URI + "share-stats/select-audits" + params;
          Alfresco.util.Ajax.jsonGet(
          {
             url: url,
             successCallback:
             {
                fn: this.displayGraph,
                scope: this
             },
             failureMessage: this._msg("Query error"),
             execScripts: true
          });
        }
        // Problème de focus avec le bouton et flash ?
        this.widgets.searchButton.blur();
      },

      /**
       * @method displayGraph Affiche le requête suite à une requête Ajax
       * @param response Réponse de la requête
       * TODO : Supprimer le flash si aucune réponse ?
       */
      displayGraph: function ConsoleAudit_displayGraph(response) {
        if(!!response.json) {
          //this.widgets.exportButton.set("disabled",false);
          this.lastRequest = response.json;
          //GetFlashData défini dans get_data.js
          var flashvars = {"get-data":"getFlashData","id":JSON.stringify(response.json)};
          var params = {};
          var attributes = {wmode: "Opaque",salign: "l",AllowScriptAccess:"always"};
          //Chemin à changer vers swf ?
          swfobject.embedSWF("/share/components/console/open_flash_chart/open-flash-chart.swf", this.id + "-chart", "75%", "400", "9.0.0","expressInstall.swf",flashvars,params,attributes);
          
        }
        else {
          var chartTag = Dom.get(this.id + "-chart").tagName.toLowerCase();
          //On remove le SWF courant. Le conteneur étant détruit, il faut le recréer ...
          if(chartTag == "embed" || chartTag == "object") { 
            swfobject.removeSWF(this.id + "-chart");
            var newChartDiv = new YAHOO.util.Element(document.createElement("div"));//this.id + "-chart"
            newChartDiv.set("id",this.id + "-chart");
            newChartDiv.appendTo(this.id + "-chart-container");
          }
          Dom.get(this.id + "-chart").innerHTML = this._msg("message.no_results");
        }
        this.widgets.searchButton.blur();
      },

      /**
       * @method convertDate
       * @param d Date au format jj/mm/aaaa
       * @return integer Timestamp unix de la date
       */
      convertDate: function ConsoleAudit_convertDate(d)
      {
        var res = 0;
        if(d.length > 0) {
          var dateArray = d.split('/');
          var dateToReturn = new Date(dateArray[2], dateArray[1]-1, dateArray[0],0,0,0);
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
      convertTimeStamp: function ConsoleAudit_convertTimeStamp(ts,exclude){
        var d = new Date(ts);
        // retour un jour en arrière en cas d'exclude
        if(exclude) {
          d.setDate(d.getDate() - 1 );
        }

        var month = (d.getMonth()+1).toString(),
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
      convertMenuValue : function ConsoleAudit_convertMenuValue(val){
        var res = null;
        if(val !== undefined && val !== "") {
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
        var type = "module";
        var date = dates ? dates : "";

        switch(action)
        {
          case "views":
            if(site == "/all") {
              type = "sites_view" + date;
            }
            else
            {
              type = "module-views" + date;
            }
          break;
          case "comment":
            if(site == "/all") {
              type = "sites_comment" + date;
            }
            else
            {
              type = "comment" + date
            }
          break;
          case "file":
            if(site == "/all") {
              type = "sites_file" + date;
            }
            else
            {
              type = "file" + date;
            }
          break;
          case null:
            type = "action" + date;
          break;
          default:
            type="module" + date;
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
      buildParams: function ConsoleAudit_buildParams(from,to,action,module,site,dates,type)
      {
        var params="?type=" + type;

        if(dates !== null && dates != ""){
          params += "&dates=" + dates;
        }
        else {
          if(from !== null && from > 0){
            params += "&from=" + from;
          }

          if(to !== null && to > 0){
            params += "&to=" + to;
          }
        }
        // if(action !== null){
          // params += "&from="+action;
        // }
        if(module !== null){
          params += "&module="+module;
        }
        if(site !== null){
          params += "&site="+site;
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
      buildTimeStampArray: function ConsoleAudit_buildTimeStampArray(pFrom, pTo, type){
        var tsArray = [], from=null, to=null;

        //Utilisation de la date courante dans si les dates sont mal saisies
        // -> Audit sur mois/semaine/jour courant
        if(pFrom == 0 && pTo == 0) {
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
        }
        else if (pFrom == 0) {
          from = new Date(pTo);
          to = new Date(pTo);
        }
        else if(pTo == 0) {
          from = new Date(pFrom);
          to = new Date(pFrom);
        }
        else {
          from = new Date(pFrom);
          to = new Date(pTo);
        }
        var res = "";

        // Créé les intervalles allant du mois de départ au mois d'arrivée INCLUS
        if(type == "-by-month") {
          tsArray.push(from.setDate(1));
          var next = new Date(from);
          next.setDate(1);
          next.setMonth(next.getMonth()+1);
          // >=
          var hasNext = (to.getTime() >= next.getTime());
          while(hasNext){
            tsArray.push(next.getTime());
            next.setMonth(next.getMonth()+1);
            hasNext = (to.getTime() >= next.getTime());
          }
          tsArray.push(next.getTime());
        }
        // Selectionne par semaine suivant from et to.
        // Les semaines de "from" et "to" sont INCLUSES
        else if(type == "-by-week") {
          /**
            //On utilise la date de départ pour récupérer tous les jours de la semaine
            to = new Date(from);
            var currentDay = from.getDay();
            //On compte le samedi (=5)
            var finalDay = 5;
            //Début de semaine
            from.setDate(from.getDate() - (currentDay - 1));
            //Fin de semaine
            to.setDate(to.getDate() + (finalDay - currentDay) +1);

            //Ajout des dates
            tsArray.push(from.getTime());
            tsArray.push(to.getTime());
          */
          //On utilise la date de départ pour récupérer tous les jours de la semaine
          var next = null,
              currentDay = from.getDay()
              hasNext = false;
          //Début de semaine
          from.setDate(from.getDate() - (currentDay - 1));
          next = new Date(from);
          tsArray.push(from.getTime());

          //Semaine suivante, on test au cas où on dépasse.
          next.setDate(from.getDate() + 7);
          hasNext = (to.getTime() >= next.getTime());
          while(hasNext){
            tsArray.push(next.getTime());
            next.setDate(next.getDate() + 7);
            hasNext = (to.getTime() >= next.getTime());
          }
          tsArray.push(next.getTime());
        }
        // Créé les intervalles allant du jour de départ au jour d'arrivée INCLUS
        else if(type == "-by-day") {
          //On ajoute la date de départ
          tsArray.push(from.getTime());

          //On ajoute 1 jour à la date de fin, pour inclure le dernier jour selectionné.
          to.setDate(to.getDate() + 1);

          //On récupère le jour suivant
          var next = new Date(from);
          next.setDate(next.getDate() + 1);

          //On vérifie qu'il ne dépasse pas la date de fin, on boucle
          var hasNext= (to > next);
          while(hasNext) {
            tsArray.push(next.getTime());
            next.setDate(next.getDate() + 1);
            hasNext = (to > next);
          }
          tsArray.push(to.getTime());
        }

        return tsArray;
      },

      onSearchClick: function ConsoleAudit_onSearchClick()
      {
        this.refreshUIState({"Time": new Date().getTime()});
      },

       //Traduction des messages
      _msg: function ConsoleAudit__msg(messageId)
      {
         return Alfresco.util.message.call(this, messageId, "Alfresco.ConsoleAudit", Array.prototype.slice.call(arguments).slice(1));
      },
      /*
      initMultiSelect: function ConsoleAudit_initMultiSelect()
      {
        a$.NO_SELECTION	= 'No selection';		// TEXT for 'No selection' when nothing selected
        a$.SELECTED		= 'Options selected';	// TEXT for 'XX Options selected' when over 1 selected
        a$.SELECT_ALL	= 'Select All';			// TEXT for 'Select All' for checkboxes
        a$.SelectAllMin	= 6;					// minimum number of options needed to show 'Select All'
        a$.WhenToUse	= 'class';				// class | multiple | all : for how to make selects become multiselects
        a$.msSeparator	= '|';					// separator for values (can be multiple characters)
      }*/

   });
})();