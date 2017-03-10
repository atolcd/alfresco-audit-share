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
      dateFormat = Alfresco.thirdparty.dateFormat;

  /**
   * UserConnections constructor.
   *
   * @param {String}
   *          htmlId The HTML id of the parent element
   * @return {AtolStatistics.UserConnections} The new UserConnections instance
   * @constructor
   */
  AtolStatistics.UserConnections = function UserConnections_constructor(htmlId) {
    AtolStatistics.UserConnections.superclass.constructor.call(this, "AtolStatistics.UserConnections", htmlId, ["button", "container", "json"]);
    Event.addListener(window, 'resize', this.onWindowResize, this, true);

    // Default value
    this.selectedGroups = [];

    return this;
  };

  YAHOO.extend(AtolStatistics.UserConnections, AtolStatistics.Tool, {
    /**
     * @attribute recentlyConnectedDelay Period (minutes) Period that defined
     *            when a user is considered as "recently connected"
     */
    recentlyConnectedDelay: 30,

    /**
     * @attribute headers
     */
    headers: [],

    /**
     * Fired by YUI when parent element is available for scripting. Component
     * initialisation, including instantiation of YUI widgets and event listener
     * binding.
     *
     * @method onReady
     */
    onReady: function UserConnections_onReady() {
      var userConnectionExecute = AtolStatistics.UserConnections.superclass.onReady.call(this);

      if (!userConnectionExecute) {
        return;
      }

      var me = this;

      // Chart type button
      this.widgets.chartTypeCriteriaButton = new YAHOO.widget.Button(this.id + "-chart-type-criteria", {
        type: "split",
        menu: this.id + "-chart-type-criteria-select",
        lazyloadmenu: false
      });
      this._setIdsForYUIMenuAndItems(this.widgets.chartTypeCriteriaButton);

      var onChartTypeMenuItemClick = function (p_sType, p_aArgs, p_oItem) {
        var sText = p_aArgs[1].cfg.getProperty("text"),
            value = p_aArgs[1].value;

        me.widgets.chartTypeCriteriaButton.value = value;
        me.widgets.chartTypeCriteriaButton.set("label", sText);
        me.execute();
      };
      this.widgets.chartTypeCriteriaButton.getMenu().subscribe("click", onChartTypeMenuItemClick);

      // headers
      this.headers["users-connected"] = Dom.get(this.id + "-users-connected-header");
      this.headers["users-never-connected"] = Dom.get(this.id + "-users-never-connected-header");
      this.headers["users-recently-connected"] = Dom.get(this.id + "-users-recently-connected-header");

      this.loadSites();
      this.loadUserGroups();
    },

    loadUserGroups: function UserConnections_loadUserGroups() {
        var url = Alfresco.constants.PROXY_URI + "share-stats/user-connections/groups";

      Alfresco.util.Ajax.jsonGet({
        url: url,
        successCallback: {
          fn: this.createUserGroupsMenu,
          scope: this
        },
        failureMessage: this.msg("label.popup.query.error"),
        execScripts: true
      });
    },

    createUserGroupsMenu: function UserConnections_createUserGroupsMenu (response) {
      if (response.json) {
        var menuButtons = [],
            me = this;

        for (var i=0, ii=response.json.length ; i < ii ; i++) {
          var current_usergroup = response.json[i];
          menuButtons.push({
            id: current_usergroup.id,
            text: current_usergroup.libelle
          });
        }
      }

      if (!this.widgets.userGroupButtonId) {
        this.widgets.userGroupButtonId = "#" + this.id + "-usergroup-criteria-select";

        $(this.widgets.userGroupButtonId).select2({
          data: menuButtons,
          placeholder: this.msg("label.menu.usergroup.all"),
          width: "160px"
        })
          .on("select2:selecting", function(e) {
            me.selectedGroups.push(e.params.args.data.id);

            me.execute();
          })
          .on("select2:unselecting", function(e) {
            var index = me.selectedGroups.indexOf(e.params.args.data.id);
            if (index > -1) {
              me.selectedGroups.splice(index, 1);
            }

            me.execute();
          });

        $(this.widgets.userGroupButtonId).prop("disabled", false);
      } else {
        // Emptying and repopulating of user groups menu when a query is call
        if (this.selectedGroups == "" || this.selectedGroups == null) {
          $(this.widgets.userGroupButtonId).empty();
        }
        $(this.widgets.userGroupButtonId).select2({
          data: menuButtons,
          placeholder: this.msg("label.menu.usergroup.all"),
          width: "160px",
        });

        $(this.widgets.userGroupButtonId).prop("disabled", false);
      }
    },

    onCSVExport: function UserConnections_onCSVExport() {
      if (this.lastRequest.params) {
        var params = this.lastRequest.params;
        params += "&type=users";
        params += "&values=" + String(this.lastRequest.values);
        params += "&interval=" + this.lastRequest.dateFilter;
        var url = Alfresco.constants.PROXY_URI + "share-stats/export-audits" + params;

        window.open(url);
      }
    },

    // Export a chart as a PNG image
    onIMGExport: function GlobalUsage_onIMGExport() {
      if (this.userChart) {
        this.exportChartAsImage(this.userChart);
      }
    },

    // Build query parameters of user groups
    addGroupsParam: function UserConnections_onUserGroups(params) {
      var userGroup = (this.widgets.userGroupButtonId) ? this.convertMenuValue(this.selectedGroups.join(',')) : "";
      if (userGroup) {
        // Encode userGroup ids
        var userGroups = [],
            userGroupsArray = userGroup.split(',');
        for (var i=0, ii=userGroupsArray.length ; i<ii ; i++) {
          userGroups.push(encodeURIComponent(userGroupsArray[i]));
        }

        params += "&groups=" + userGroups.join(',');
      }
      return params;
    },

    prepareRecentlyConnectedUsersRequest: function UserConnections_prepareRecentlyConnectedUsersRequest() {
      // Retrieve variables from UI
      var site = this.convertMenuValue(this.widgets.siteButton.value),
          currentDate = new Date(),
          params = "";

      params = "?type=users-recently-connected";
      params += "&to=" + currentDate.getTime();
      params += "&from=" + currentDate.setMinutes(currentDate.getMinutes() - this.recentlyConnectedDelay);
      if (site) {
        if (site.indexOf(',') >= 0) {
          // Encode site ids
          var sites = [],
              sitesArray = site.split(',');
          for (var i=0, ii=sitesArray.length ; i<ii ; i++) {
            sites.push(encodeURIComponent(sitesArray[i]));
          }

          params += "&sites=" + sites.join(',');
        } else {
          params += "&site=" + encodeURIComponent(site);
        }
      }

      params = this.addGroupsParam(params);
      this.executeUserRequest(params, "users-recently-connected");
    },

    prepareUserRequest: function UserConnections_prepareUserRequest(type) {
      // Retrieve variables from UI
      var dateFilter = this.options.currentDateFilter,
          site = this.convertMenuValue(this.widgets.siteButton.value),
          params = "";

      // Date range table
      tsArray = this.buildTimeStampArray();
      params = "?type=" + type;
      params += "&from=" + tsArray[0];
      params += "&to=" + tsArray[tsArray.length - 1];
      if (site) {
        if (site.indexOf(',') >= 0) {
          // Encode site ids
          var sites = [],
              sitesArray = site.split(',');
          for (var i=0, ii=sitesArray.length ; i<ii ; i++) {
            sites.push(encodeURIComponent(sitesArray[i]));
          }

          params += "&sites=" + sites.join(',');
        } else {
          params += "&site=" + encodeURIComponent(site);
        }
      }

      params = this.addGroupsParam(params);
      this.executeUserRequest(params, type);
    },

    executeUserRequest: function UserConnections_executeUserRequest(params, type) {
      var displayUsers = function (response) {
        var users = response.json.items,
            el = Dom.get(this.id + "-" + type);

        if (users && users.length) {
          var context = Alfresco.constants.URL_PAGECONTEXT,
              html = "";

          for (var i=0, ii=users.length ; i<ii ; i++) {
            var user = users[i];
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

    onSearch: function UserConnections_onSearch() {
      // Retrieve variables from UI
      var dateFilter = this.options.currentDateFilter,
        site = this.convertMenuValue(this.widgets.siteButton.value),
        tsString = "",
        params = "";

      // Date range table
      if (dateFilter) {
        // Labels update
        var tsArray = this.buildTimeStampArray();
        this.updateUsersLabels(tsArray);
        tsString = tsArray.toString();
      }

      params = "?type=users-count";
      params += "&dates=" + tsString;

      // Build query parameters of sites
      if (site) {
        if (site.indexOf(',') >= 0) {
          // Encode site ids
          var sites = [],
              sitesArray = site.split(',');
          for (var i=0, ii=sitesArray.length ; i<ii ; i++) {
            sites.push(encodeURIComponent(sitesArray[i]));
          }

          params += "&sites=" + sites.join(',');
        } else {
          params += "&site=" + encodeURIComponent(site);
        }
      }
      params = this.addGroupsParam(params);
      this.lastRequest.params = params;
      this.lastRequest.dateFilter = dateFilter;

      var url = Alfresco.constants.PROXY_URI + "share-stats/select-users" + this.lastRequest.params;
      Alfresco.util.Ajax.jsonGet({
        url: url,
        successCallback: {
          fn: this.displayUserGraph,
          scope: this
        },
        failureMessage: this.msg("label.popup.query.error"),
        execScripts: true,
        additionalsParams: {
          chartType: this.widgets.chartTypeCriteriaButton.value || '',
          type: "count",
          site: site,
          siteTitle: this.sites[site] || '',
          tsString: tsString
        }
      });
    },

    onWindowResize: function UserConnections_onWindowResize() {
      if (this.userChart) {
        var resizeParameters = {
          currentFilter: this.options.currentDateFilter,
          additionalsParams: {
            tsString: this.buildTimeStampArray().toString()
          },
          chartDomId: this.id + '-chart'
        };
        this.userChart.categories(this.buildBarChartXLabels(resizeParameters, this.options.chartLabelSizeMin));
      }
    },

    getMessage: function UserConnections_getMessage(messageId, prefix) {
      var msg = (prefix) ? prefix + messageId : messageId;
      var res = Alfresco.util.message.call(null, msg, "AtolStatistics.UserConnections", Array.prototype.slice.call(arguments).slice(2));
      res = (res.search("graph.label") == 0) ? messageId : res;
      return res;
    },

    // Chart Displaying with C3
    displayUserGraph: function UserConnections_displayUserGraph(response) {
      if (response.json) {
        var displayParameters = {
            currentFilter: this.options.currentDateFilter,
            additionalsParams: response.config.additionalsParams,
            chartDomId: this.id + "-chart"
          },
          labelUserConnection = this.msg("tool.user-connections.label"),
          chartArguments = {
              bindto: '#' + displayParameters.chartDomId,
              data: {
                columns: [
                  [labelUserConnection].concat(response.json.values)
                ]
              },
              size: {
                height: 450
              },
              grid: {
                x: { show: true },
                y: { show: true }
              },
              title: {
                text: this.buildTitle(displayParameters)
              },
              point: { show: false },
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
          };

          switch (displayParameters.additionalsParams.chartType) {
            default :
            case "bar":
              chartArguments.data.type = 'bar';
            break;
            case "line":
              chartArguments.point.show = true;
            break;
          };

          // Recovery of the connection values and reactivation of the export button
          this.lastRequest.values = response.json.values;
          this.widgets.exportButton.set("disabled", false);

          // build chart
          this.userChart = c3.generate(chartArguments);
      } else {
        if (this.userChart) {
          this.userChart.unload(); // chart unloading
        }
        this.widgets.exportButton.set("disabled", true);
      }
    },

    /**
     * @method buildParams This function is used to build GET query request
     * @return string - url params
     */
    buildParams: function UserConnections_buildParams() {
      // TODO:
      return "";
    },

    updateUsersLabels: function UserConnections_updateUsersLabels(tsArray) {
      var connectedLabel = "",
        neverConnectedLabel = "";

      switch (this.options.currentDateFilter) {
      case "days":
        var date = new Date(tsArray[0]),
            today = new Date();

        if (date.getMonth() == today.getMonth() && date.getDate() == today.getDate() && date.getFullYear() == today.getFullYear()) {
          connectedLabel = this.msg("label.users.connected.today");
          neverConnectedLabel = this.msg("label.users.never-connected.today");
        } else {
          var day = dateFormat(date, AtolStatistics.dateFormatMasks.mediumDay); // dddd
                                                                                // dd/mm
          connectedLabel = this.msg("label.users.connected." + this.options.currentDateFilter, day);
          neverConnectedLabel = this.msg("label.users.never-connected." + this.options.currentDateFilter, day);
        }
        break;

      case "weeks":
        var from = dateFormat(new Date(tsArray[0]), AtolStatistics.dateFormatMasks.shortDate), // dd/mm/yyyy
            to   = dateFormat(new Date(tsArray[tsArray.length - 2]), AtolStatistics.dateFormatMasks.shortDate); // dd/mm/yyyy

        connectedLabel = this.msg("label.users.connected." + this.options.currentDateFilter, from, to);
        neverConnectedLabel = this.msg("label.users.never-connected." + this.options.currentDateFilter, from, to);
        break;

      case "months":
        var month = dateFormat(new Date(tsArray[0]), AtolStatistics.dateFormatMasks.monthYear); // mmmm
                                                                                                // yyyy
        connectedLabel = this.msg("label.users.connected." + this.options.currentDateFilter, month);
        neverConnectedLabel = this.msg("label.users.never-connected." + this.options.currentDateFilter, month);
        break;

      case "years":
        var date = new Date(tsArray[0]);
        connectedLabel = this.msg("label.users.connected." + this.options.currentDateFilter, date.getFullYear());
        neverConnectedLabel = this.msg("label.users.never-connected." + this.options.currentDateFilter, date.getFullYear());
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

    execute: function UserConnections_execute() {
      this.prepareUserRequest("users-connected");
      this.prepareUserRequest("users-never-connected");
      this.prepareRecentlyConnectedUsersRequest();

      AtolStatistics.UserConnections.superclass.execute.call(this);
    }
  });
})();