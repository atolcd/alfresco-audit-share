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
if (typeof AtolStatistics == "undefined" || !AtolStatistics) {
  var AtolStatistics = {};
}

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
   * @param {String} htmlId The HTML id of the parent element
   * @return {AtolStatistics.GlobalUsage} The new GlobalUsage instance
   * @constructor
   */
  AtolStatistics.GlobalUsage = function GlobalUsage_constructor(htmlId) {
    AtolStatistics.GlobalUsage.superclass.constructor.call(this, "AtolStatistics.GlobalUsage", htmlId, ["button", "container", "json"]);
    Event.addListener(window, 'resize', this.onWindowResize, this, true);
    // Default values
    this.options.limit = 5;
    this.options.popularity = 25;
    this.popularityCharts = {};
    this.options.barChartColors = [];
    this.selectedNodeTypes = [];

    // Declaration of the colors used by charts
    var red = "#EE1C2F",
      blue = "#19ABEA", darkBlue = "#1B5AF9",
      green = "#7CBC28", darkGreen = "#0A9200",
      orange = "#FF9900", lightOrange = "#FFC600",
      darkOrange = "#FF692B";

    // Blog
    this.options.barChartColors["blog.postview"] = blue;
    this.options.barChartColors["blog.blog-create"] = darkGreen;
    this.options.barChartColors["blog.blog-delete"] = red;
    this.options.barChartColors["blog.blog-update"] = orange;

    // Document Library
    this.options.barChartColors["document.details"] = blue;
    this.options.barChartColors["document.download"] = darkBlue;
    this.options.barChartColors["document.create"] = green;
    this.options.barChartColors["document.file-added"] = darkGreen;
    this.options.barChartColors["document.file-deleted"] = red;
    this.options.barChartColors["document.file-updated"] = darkOrange;
    this.options.barChartColors["document.inline-edit"] = lightOrange;
    this.options.barChartColors["document.update"] = orange;

    // Wiki
    this.options.barChartColors["wiki.page"] = blue;
    this.options.barChartColors["wiki.create-post"] = darkGreen;
    this.options.barChartColors["wiki.delete-post"] = red;
    this.options.barChartColors["wiki.update-post"] = orange;

    // Discussions
    this.options.barChartColors["discussions.topicview"] = blue;
    this.options.barChartColors["discussions.discussions-create"] = darkGreen;
    this.options.barChartColors["discussions.discussions-deleted"] = red;
    this.options.barChartColors["discussions.discussions-update"] = orange;

    // Charts
    this.options.barChartColors["volumetry"] = blue;
    this.options.barChartColors["users"] = blue;
    this.options.barChartColors["most-popular"] = red;
    this.options.barChartColors["less-popular"] = blue;

    return this;
  };

  YAHOO.extend(AtolStatistics.GlobalUsage, AtolStatistics.Tool, {
    /**
     * Fired by YUI when parent element is available for scripting.
     * Component initialisation, including instantiation of YUI widgets and event listener binding.
     *
     * @method onReady
     */

    onReady: function GlobalUsage_onReady() {
      var globalExecute = AtolStatistics.GlobalUsage.superclass.onReady.call(this);

      if (!globalExecute) {
        return;
      }

      // Export menu: add menu items
      var exportMenu = this.widgets.exportButton.getMenu();
      if (exportMenu) {
        exportMenu.addItems([
          { text: this.msg("label.export.csv.most_read"), value: "mostread", disabled: false, id: this.widgets.exportButton.get("id") + "-menu-item-mostread" },
          { text: this.msg("label.export.csv.most_update"), value: "mostupdated", disabled: false, id: this.widgets.exportButton.get("id") + "-menu-item-mostupdated"  }
        ]);
      }

      var me = this;

      this.widgets.moduleCriteriaButton = new YAHOO.widget.Button(this.id + "-module-criteria", {
        type: "split",
        menu: this.id + "-module-criteria-select",
        lazyloadmenu: false
      });
      this.widgets.moduleCriteriaButton.getMenu().cfg.setProperty("zIndex", 4);
      this.widgets.moduleCriteriaButton.value = "document";
      this._setIdsForYUIMenuAndItems(this.widgets.moduleCriteriaButton);

      this.widgets.actionCriteriaButton = new YAHOO.widget.Button(this.id + "-action-criteria", {
        type: "split",
        menu: this.id + "-action-criteria-select",
        lazyloadmenu: false
      });
      this.widgets.actionCriteriaButton.value = "read";
      this._setIdsForYUIMenuAndItems(this.widgets.actionCriteriaButton);

      // Listeners on menu click
      // "Module" filter
      var onModulesMenuItemClick = function (p_sType, p_aArgs) {
        var sText = p_aArgs[1].cfg.getProperty("text"),
            value = p_aArgs[1].value;

        // Update export menu
        if (me.widgets.exportButton && value) {
          var disabledCSVExportBtn = !(value == "document");
          var exportMenuElts = me.widgets.exportButton.getMenu().getItems();
          if (exportMenuElts) {
            for (var i=0, ii=exportMenuElts.length ; i<ii ; i++) {
              if (exportMenuElts[i].value == "mostread" || exportMenuElts[i].value == "mostupdated") {
                exportMenuElts[i].cfg.setProperty("disabled", disabledCSVExportBtn);
              }
            }
          }
        }

        me.widgets.moduleCriteriaButton.value = value;
        me.widgets.moduleCriteriaButton.set("label", sText);
        me.execute();
      };
      this.widgets.moduleCriteriaButton.getMenu().subscribe("click", onModulesMenuItemClick);

      // Add separator before last item
      var itemsCount = this.widgets.moduleCriteriaButton.getMenu().getItems().length;
      if (itemsCount > 0) {
        Dom.addClass(this.widgets.moduleCriteriaButton.getMenu().getItem(itemsCount - 1).element, "menu-separator");
      }

      // "Action" filter
      var onActionsMenuItemClick = function (p_sType, p_aArgs) {
        var sText = p_aArgs[1].cfg.getProperty("text"),
            value = p_aArgs[1].value;

        me.widgets.actionCriteriaButton.value = value;
        me.widgets.actionCriteriaButton.set("label", sText);
        me.execute();
      };
      this.widgets.actionCriteriaButton.getMenu().subscribe("click", onActionsMenuItemClick);

      this.loadSites();

      this.initNodeTypesMenu();
    },

    loadNodeTypes: function GlobalUsage_loadNodeTypes() {
      var module = this.convertMenuValue(this.widgets.moduleCriteriaButton.value);

      // Call the Webscript only when the document library is selected
      if (module == "document") {
        var url = Alfresco.constants.PROXY_URI + "share-stats/select-nodetypes" + this.nodeTypeRequestParams;
        Alfresco.util.Ajax.jsonGet({
          url: url,
          successCallback: {
            fn: this.createNodeTypesMenu,
            scope: this
          },
          failureMessage: this.msg("label.popup.query.error"),
          execScripts: true
        });
      } else {
        if (this.widgets.nodeTypeButton) {
          $(this.widgets.nodeTypeButton).prop("disabled", true);
        }
      }
    },

    initNodeTypesMenu: function GlobalUsage_initNodeTypesMenu () {
      var me = this;

      if (!this.widgets.nodeTypeButton) {
        this.widgets.nodeTypeButton = "#" + this.id + "-nodetype-criteria-select";

        $(this.widgets.nodeTypeButton).select2({
          data: [],
          placeholder: this.msg("label.menu.nodetype.all"),
          width: "200px"
        }).on("select2:selecting", function(e) {
          me.selectedNodeTypes.push(e.params.args.data.id);
          me.execute();
        }).on("select2:unselecting", function(e) {
          var index = me.selectedNodeTypes.indexOf(e.params.args.data.id);
          if (index > -1) {
            me.selectedNodeTypes.splice(index, 1);
          }

          me.execute();
        });

        $(this.widgets.nodeTypeButton).prop("disabled", false);
      }
    },

    createNodeTypesMenu: function GlobalUsage_createNodeTypesMenu (response) {
      var menuButtons = [];

      if (response.json) {
        for (var i=0, ii=response.json.length ; i < ii ; i++) {
          var current_nodetype = response.json[i];
          menuButtons.push({
            id: current_nodetype.value,
            text: current_nodetype.label
          });
        }
      }

      // Emptying and repopulating of node types menu when a query is call
      if (this.selectedNodeTypes == "" || this.selectedNodeTypes == null) {
        $(this.widgets.nodeTypeButton).empty();
      }
      $(this.widgets.nodeTypeButton).select2({
        data: menuButtons,
        placeholder: this.msg("label.menu.nodetype.all"),
        width: "200px",
      });

      $(this.widgets.nodeTypeButton).prop("disabled", false);
    },

    onCSVExport: function GlobalUsage_onCSVExport() {
      if (this.lastRequest.params) {
        var params = this.lastRequest.params;
        params += "&interval=" + this.lastRequest.dateFilter;
        var url = Alfresco.constants.PROXY_URI + "share-stats/export-audits" + params;
        window.open(url);
      }
    },

    // Export a chart as a PNG image
    onIMGExport: function GlobalUsage_onIMGExport() {
      if (this.globalChart) {
        this.exportChartAsImage(this.globalChart);
      }
    },

    // For export mostread chart (csv)
    mostread: function GlobalUsage_mostread() {
      if(this.options&&this.options.paramsmostread){
        var url = Alfresco.constants.PROXY_URI + "share-stats/export-audits" + this.options.paramsmostread;
        window.open(url);
      }
    },

    // For export most updated chart (csv)
    mostupdated: function GlobalUsage_mostupdated() {
      if(this.options&&this.options.paramsmostupdated){
        var url = Alfresco.constants.PROXY_URI + "share-stats/export-audits" + this.options.paramsmostupdated;
        window.open(url);
      }
    },

    onSearch: function GlobalUsage_onSearch() {
      // Retrieve variables from UI
      var action = this.convertMenuValue(this.widgets.actionCriteriaButton.value),
          module = this.convertMenuValue(this.widgets.moduleCriteriaButton.value),
          dateFilter = this.options.currentDateFilter,
          site = this.convertMenuValue(this.widgets.siteButton.value),
          type = action,
          tsArray = this.buildTimeStampArray(),
          from = tsArray[0],
          to = tsArray[tsArray.length - 1],
          nodeType = (this.widgets.nodeTypeButton) ? this.convertMenuValue(this.selectedNodeTypes.join(',')) : "",
          tsString;

      // Date range table
      if (dateFilter) {
        tsString = this.buildTimeStampArray().toString();
      }

      // Build query parameters
      this.nodeTypeRequestParams = this.buildParams(module, site, null, type, from, to, null, nodeType); // Query parameters of nodetypes
      this.lastRequest.params = this.buildParams(module, site, tsString, type, null, null, null, nodeType);
      this.lastRequest.dateFilter = dateFilter;

      var url = Alfresco.constants.PROXY_URI + "share-stats/select-audits" + this.lastRequest.params;
      Alfresco.util.Ajax.jsonGet({
        url: url,
        successCallback: {
          fn: this.displayGlobalUsageGraph,
          scope: this
        },
        failureMessage: this.msg("label.popup.query.error"),
        execScripts: true,
        additionalsParams: {
          chartType: "bar",
          type: type,
          tsString: tsString
        }
      });
    },

    // Refresh the chart page after resizing
    onWindowResize: function GlobalUsage_onWindowResize() {
      if (this.globalChart) {
        var resizeParameters = {
          currentFilter: this.options.currentDateFilter,
          additionalsParams: {
            tsString: this.buildTimeStampArray().toString()
          },
          chartDomId: this.id + '-chart'
        };
        this.globalChart.categories(this.buildBarChartXLabels(resizeParameters, this.options.chartLabelSizeMin));
      }

      // Remove the last width attribute of the svg tag. Because the width resizing is not native on FireFox
      if (this.popularityCharts) {
        for (var chartId in this.popularityCharts) {
          if (this.popularityCharts.hasOwnProperty(chartId)) {
            var theChart = this.popularityCharts[chartId];
            if (theChart.element && theChart.element.firstChild) {
              theChart.element.firstChild.removeAttribute("width");
            }
          }
        }
      }
    },

    getMessage: function GlobalUsage_getMessage(messageId, prefix) {
      var msg = (prefix) ? prefix + messageId : messageId;
      var res = Alfresco.util.message.call(null, msg, "AtolStatistics.GlobalUsage", Array.prototype.slice.call(arguments).slice(2));
      res = (res.search("graph.label") == 0) ? messageId : res;
      return res;
    },

    // Display the main graph
    displayGlobalUsageGraph: function GlobalUsage_displayGlobalUsageGraph(response) {
      if (response.json) {
        var displayParameters = {
            currentFilter: this.options.currentDateFilter,
            additionalsParams: response.config.additionalsParams,
            chartDomId: this.id + "-chart"
          },
          chartArguments = {
              bindto: '#' + displayParameters.chartDomId,
              data: {
                columns: [],
                type: 'bar',
                colors: {}
              },
              size: {
                height: 450
              },
              grid: {
                x: { show: true },
                y: { show: true }
              },
              title: {
                text: this.getMessage(displayParameters.additionalsParams.type, "graph.title.") + this.buildDateTitle(displayParameters)
              },
              point: { show: false },
              tooltip: {
                grouped: false
              },
              axis: {
                x: {
                  type: 'category',
                  categories: this.buildBarChartXLabels(displayParameters, this.options.chartLabelSizeMin),
                  tick: {
                    multiline: false,
                    outer: false
                  }
                },
                y: {
                  tick: { format: d3.format(",d") }
                }
              }
          },
          i, ii, j, jj;

        // Recovery datas for graph displaying
        var jsonResults = [];

        for (i = 0, ii = response.json.items.length ; i < ii ; i++) {
          var jsonItem = response.json.items[i];
          if (jsonItem.totalResults > 0) {
            for (j = 0, jj = jsonItem.totalResults; j < jj; j++) {
              var labelParam = jsonItem.items[j].target;

              if (!jsonResults[labelParam]) {
                jsonResults[labelParam] = [];
                jsonResults[labelParam][i] = jsonItem.items[j].count;
              } else {
                jsonResults[labelParam][i] = jsonItem.items[j].count;
              }
            }
          }
        }

        for (var resultId in jsonResults) {
          if (jsonResults.hasOwnProperty(resultId)) {
            var value_obj = [this.getMessage(resultId, "graph.label.")];
            for (j = 0, jj = response.json.items.length; j < jj; j++) {
              if (!jsonResults[resultId][j]) {
                jsonResults[resultId][j] = 0;
              }
            }

            for (i = 0, ii = jsonResults[resultId].length; i < ii; i++) {
              value_obj.push(jsonResults[resultId][i]);
            }
            chartArguments.data.colors[value_obj[0]] = this.options.barChartColors[resultId];
            chartArguments.data.columns.push(value_obj);
          }
        }

          // Recovery of the connection values and reactivation of the export button
          this.widgets.exportButton.set("disabled", false);

          // build chart
          this.globalChart = c3.generate(chartArguments);

      } else {
        if (this.globalChart) {
          this.globalChart.unload(); // chart unloading
        }
        this.widgets.exportButton.set("disabled", true);
      }
    },

    getByPopularity: function GlobalUsage_getByPopularity(type) {
      var site = this.convertMenuValue(this.widgets.siteButton.value),
          module = this.convertMenuValue(this.widgets.moduleCriteriaButton.value),
          tsArray = this.buildTimeStampArray(),
          tsString = tsArray.toString(),
          from = tsArray[0],
          to = tsArray[tsArray.length - 1],
          nodeType = (this.widgets.nodeTypeButton) ? this.convertMenuValue(this.selectedNodeTypes.join(',')) : "",
          params;

      params = this.buildParams(module, site, null, type, from, to, this.options.limit, nodeType);

      // Build query parameters
      if ("mostread" === type) {
        this.options.paramsmostread = params;
      } else if ("mostupdated" === type) {
        this.options.paramsmostupdated = params;
      }

      var url = Alfresco.constants.PROXY_URI + "share-stats/select-audits" + params;
      Alfresco.util.Ajax.jsonGet({
        url: url,
        successCallback: {
          fn: this.displayPopularityGraph,
          scope: this
        },
        failureMessage: this.msg("label.popup.query.error"),
        execScripts: true,
        additionalsParams: {
          chartType: "bar",
          type: type,
          tsString: tsString,
          urlTemplate : this.getTemplateUrl()
        }
      });
    },

    getTemplateUrl: function GlobalUsage_getTemplateUrl() {
      var baseUrl = window.location.protocol + "//" + window.location.host + Alfresco.constants.URL_PAGECONTEXT + "site/{site}/";

      return {
        "documentLibrary" : baseUrl + "document-details?nodeRef={nodeRef}",
        "wiki": baseUrl + "wiki-page?title={id}&listViewLinkBack=true",
        "blog": baseUrl + "blog-postview?postId={id}&listViewLinkBack=true",
        "discussions": baseUrl + "discussions-topicview?topicId={id}&listViewLinkBack=true",
        "": window.location.protocol + "//" + window.location.host + Alfresco.constants.URL_PAGECONTEXT + "document-details?nodeRef={nodeRef}"
      };
    },

    // Display the two smallest graphs
    displayPopularityGraph: function GlobalUsage_displayPopularityGraph(response) {
      if (response.json) {
        var displayParameters = {
          currentFilter: this.options.currentDateFilter,
          additionalsParams: response.config.additionalsParams
        },
        me = this,
        colors = ['#1f77b4', '#aec7e8', '#ff7f0e', '#ffbb78', '#2ca02c',
                  '#98df8a', '#d62728', '#ff9896', '#9467bd', '#c5b0d5'],
        chartsPopularityArguments = {
          data: {
            selection: { enabled: true },
            json: [],
            keys: {},
            type: 'bar',
            color: function (color, d) {
              return colors[d.x];
            },
            onclick: function(barParams) {
              var item = response.json.items[barParams.x];

              var body = '<div class="node-details-popup">';
              body += '<p><label>' + me.getMessage("label.popup.filename") + '</label>' + item.name + '</p>';
              body += (item.siteTitle) ? '<p><label>' + me.getMessage("label.popup.type") + '</label>' + me.getMessage("site.component." + item.siteComponent) + '</p>' : '';
              body += (item.siteTitle) ? '<p><label>' + me.getMessage("label.menu.site") + '</label>' + item.siteTitle + '</p>' : '';
              body += '<p><label>' + me.getMessage("label.popup.hits") + '</label>' + item.popularity + '</p>';
              body += '</div>';

              Alfresco.util.PopupManager.displayPrompt({
                title: item.name,
                text: body,
                close: true,
                noEscape: true,
                buttons: [{
                  text: me.getMessage("button.go-to-node-page"),
                  handler:{
                    fn: function() {
                      var url = YAHOO.lang.substitute(displayParameters.additionalsParams.urlTemplate[item.siteComponent], {
                        nodeRef : item.nodeRef,
                        site : item.site
                      });
                      window.open(url);
                      this.destroy();
                    },
                    obj: item
                  }
                }, {
                  text: me.getMessage("button.cancel"),
                  handler: function () {
                    this.destroy();
                  },
                  isDefault: true
                }]
              });
            }
          },
          size: {
            height: 200
          },
          grid: {
            x: { show: true },
            y: { show: true }
          },
          title: {},
          point: { show: false },
          axis: {
            rotated: true,
            x: {
              type: 'category',
              categories: [],
              tick: {
                multiline: false,
                outer: false
              }
            },
            y: {
              tick: { format: d3.format(",d") }
            }
          },
          tooltip: {
            grouped: false,
            contents: function (barParams, defaultTitleFormat, defaultValueFormat, color) {
              barParams[0].name = response.json.items[barParams[0].x].name;
              return this.getTooltipContent(barParams, defaultTitleFormat, defaultValueFormat, color);
            },
            format: {
              title: function (d) {
                return (response.json.items[d].siteTitle) ? me.getMessage("label.menu.site") + response.json.items[d].siteTitle : response.json.items[d].name;
              }
            }
          },
          legend: {
            show: false
          }
        };

        // Generation of the charts
        chartsPopularityArguments.bindto = '#' + this.id + '-' + response.config.additionalsParams.type;

        for (var i=0, ii=response.json.items.length ; i<ii ; i++) {
          var value_obj = response.json.items[i];
          chartsPopularityArguments.data.json.push(value_obj);
          if (chartsPopularityArguments.data.json[i].displayName.length > this.options.popularity) {
            chartsPopularityArguments.data.json[i].displayName = chartsPopularityArguments.data.json[i].displayName.substr(0, this.options.popularity) + " ...";
          }
        }
        chartsPopularityArguments.data.keys = {x: 'displayName', value: ['popularity']};

        chartsPopularityArguments.title.text = (i > 1) ? this.getMessage(response.config.additionalsParams.type, "graph.label.", response.json.items.length) :
            this.getMessage(response.config.additionalsParams.type, "graph.label.", "");

        this.popularityCharts[response.config.additionalsParams.type] = c3.generate(chartsPopularityArguments);
      } else {
        if (this.popularityCharts[response.config.additionalsParams.type]) {
          this.popularityCharts[response.config.additionalsParams.type].unload(); // chart unloading
        }
        this.widgets.exportButton.set("disabled", true);
      }
    },

    /**
     * @method buildParams
     *         This function is used to build GET query request
     *
     * @param from Timestamp (string) - date from
     * @param to Timestamp (string) - date to
     * @param module - selected module
     * @param dates - selected dates
     * @param type - query type
     * @param limit - results limit
     * @param nodetype - selected node type

     * @return string - url params
     */
    buildParams: function GlobalUsage_buildParams(module, site, dates, type, from, to, limit, nodetype) {
      var params = "?type=" + type,
          i, ii;

      if (dates !== null && dates != "") {
        params += "&dates=" + dates;
      }
      if (module !== null) {
        if (module === "all") {
          var moduleValues = [],
              items = this.widgets.moduleCriteriaButton.getMenu().getItems();
          for (i=0, ii=items.length ; i<ii ; i++) {
            var item = items[i];
            if (item.value != "") {
              moduleValues.push(item.value);
            }
          }

          params += "&modules=" + moduleValues.join(',') + "&combined=true";
        } else {
          params += "&module=" + module;
        }
      }
      if (site !== null) {
        if (site === '*') {
          params += "&sites=*";
        } else {
          params += "&site=" + encodeURIComponent(site);
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
      if (nodetype) {
        if (nodetype.indexOf(',') >= 0) {
          // Encode nodetype ids
          var nodetypes = [],
          nodetypesArray = nodetype.split(',');
          for (i=0, ii=nodetypesArray.length ; i<ii ; i++) {
            nodetypes.push(encodeURIComponent(nodetypesArray[i]));
          }

          params += "&nodeTypes=" + nodetypes.join(',');
        } else {
          params += "&nodeType=" + encodeURIComponent(nodetype);
        }
      }

      return params;
    },

    execute: function GlobalUsage_execute() {
      this.getByPopularity("mostupdated");
      this.getByPopularity("mostread");

      AtolStatistics.GlobalUsage.superclass.execute.call(this);

      this.loadNodeTypes();
    }
  });
})();