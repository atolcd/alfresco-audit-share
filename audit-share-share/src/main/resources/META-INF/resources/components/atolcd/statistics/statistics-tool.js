/*
 * Copyright (C) 2018 Atol Conseils et Développements.
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

// AtolStatistics top-level util namespace.
AtolStatistics.util = AtolStatistics.util || {};

// AtolStatistics top-level dateFormatMasks namespace.
AtolStatistics.dateFormatMasks = AtolStatistics.dateFormatMasks || {};

AtolStatistics.dateFormatMasks = {
  fullDate: Alfresco.util.message("statistics.date-format.fullDate"),
  mediumDate: Alfresco.util.message("statistics.date-format.mediumDate"),
  shortDate: Alfresco.util.message("statistics.date-format.shortDate"),
  monthYear: Alfresco.util.message("statistics.date-format.monthYear"),
  fullDay: Alfresco.util.message("statistics.date-format.fullDay"),
  mediumDay: Alfresco.util.message("statistics.date-format.mediumDay"),
  shortDay: Alfresco.util.message("statistics.date-format.shortDay"),
  fullMonth: Alfresco.util.message("statistics.date-format.fullMonth"),
  fullHour: Alfresco.util.message("statistics.date-format.fullHour"),
  shortHour: Alfresco.util.message("statistics.date-format.shortHour")
};

AtolStatistics.util.fileSizes = {
  'BYTES_B' : 1,
  'BYTES_KB': 1024,
  'BYTES_MB': 1048576,
  'BYTES_GB': 1073741824,
  'BYTES_TB': 1099511627776
};

AtolStatistics.util.fileSizeMessages = {
  'BYTES_B' : Alfresco.util.message("size.bytes"),
  'BYTES_KB': Alfresco.util.message("size.kilobytes"),
  'BYTES_MB': Alfresco.util.message("size.megabytes"),
  'BYTES_GB': Alfresco.util.message("size.gigabytes"),
  'BYTES_TB': Alfresco.util.message("size.terabytes")
};

AtolStatistics.util.roundNumber = function (number, digits) {
  var multiple = Math.pow(10, digits);
  return Math.round(number * multiple) / multiple;
}

AtolStatistics.util.formatFileSize = function (fileSize, unit) {
  if (typeof fileSize == "string") {
    fileSize = parseInt(fileSize, 10);
  }

  if (unit) {
    return  {
      unit: unit,
      value: AtolStatistics.util.roundNumber(fileSize / AtolStatistics.util.fileSizes[unit], 2),
      message: AtolStatistics.util.fileSizeMessages[unit]
    };
  }

  if (fileSize < AtolStatistics.util.fileSizes.BYTES_KB) {
    return  {
      unit: "BYTES_B",
      value: fileSize,
      message: AtolStatistics.util.fileSizeMessages["BYTES_B"]
    };
  }
  else if (fileSize < AtolStatistics.util.fileSizes.BYTES_MB) {
    return  {
      unit: "BYTES_KB",
      value: AtolStatistics.util.roundNumber(fileSize / AtolStatistics.util.fileSizes.BYTES_KB, 2),
      message: AtolStatistics.util.fileSizeMessages["BYTES_KB"]
    };
  }
  else if (fileSize < AtolStatistics.util.fileSizes.BYTES_GB) {
    return  {
      unit: "BYTES_MB",
      value: AtolStatistics.util.roundNumber(fileSize / AtolStatistics.util.fileSizes.BYTES_MB, 2),
      message: AtolStatistics.util.fileSizeMessages["BYTES_MB"]
    };
  }
  else if (fileSize < AtolStatistics.util.fileSizes.BYTES_TB) {
    return  {
      unit: "BYTES_GB",
      value: AtolStatistics.util.roundNumber(fileSize / AtolStatistics.util.fileSizes.BYTES_GB, 2),
      message: AtolStatistics.util.fileSizeMessages["BYTES_GB"]
    };
  }

  return  {
    unit: "BYTES_TB",
    value: AtolStatistics.util.roundNumber(fileSize / AtolStatistics.util.fileSizes.BYTES_TB, 2),
    message: AtolStatistics.util.fileSizeMessages["BYTES_TB"]
  };
};

/**
 * Tool tool component.
 *
 * @namespace AtolStatistics
 * @class AtolStatistics.Tool
 */
(function () {
  /**
   * YUI Library aliases
   */
  var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event;

  /**
   * Tool constructor.
   *
   * @param {String} htmlId The HTML id of the parent element
   * @return {AtolStatistics.Tool} The new Tool instance
   * @constructor
   */
  AtolStatistics.Tool = function Tool_constructor(htmlId) {
    AtolStatistics.Tool.superclass.constructor.apply(this, arguments);

    // @Override getWeek() date function
    Date.prototype.getWeek = function() {
     var onejan = new Date(this.getFullYear(), 0, 1);
     return Math.ceil((((this - onejan) / 86400000) + onejan.getDay() + 1) / 7);
    };

    return this;
  };

  YAHOO.extend(AtolStatistics.Tool, Alfresco.component.Base, {
    options: {
      /**
       * @option currentDateFilter
       *
       * Current date filter: "days", "weeks", "months" or "years"
       * default value: "weeks"
       */
      currentDateFilter: "weeks"
    },

    /**
     * @attribute lastRequest
     *
     * Cache (last executed query)
     * Use for CSV export
     */
    lastRequest: {
      params: null,
      data: null,
      from: null,
      to: null
    },

    /**
     * @attribute endDatesArray
     *
     * Dates used by the charts
     */
    endDatesArray: [],

    /**
     * @attribute sites
     *
     * Sites informations (id / title)
     */
    sites: {},

    /**
     * Fired by YUI when parent element is available for scripting.
     * Component initialisation, including instantiation of YUI widgets and event listener binding.
     *
     * @method onReady
     */
    onReady: function Tool_onReady() {
      // Disable all of the DIV of the page for IE8 and under
      if (YAHOO.env.ua.ie && ((!document.documentMode && YAHOO.env.ua.ie < 9) || document.documentMode < 9)) {
        if (Dom.get("alfresco-statistics")) {
          YAHOO.util.Dom.setStyle('alfresco-statistics', 'display', 'none');
        }

        var body = '<div class="node-details-popup">';
        body += '<p><label>' + this.getMessage("label.compatibility-popup.textBegin") + '</label></p>';
        body += '<p><label>' + this.getMessage("label.compatibility-popup.textEnd") + '</label></p>';
        body += '</div>';

        // Call the Pop-up
        Alfresco.util.PopupManager.displayPrompt({
          title: this.getMessage("label.compatibility-popup.title"),
          text: body,
          close: true,
          noEscape: true,
          buttons: [{
            text: this.getMessage("button.ok"),
            handler: function () {
              this.destroy();
            },
            isDefault: true
          }]
        });
        return false;
      }

      var me = this;
      this.setupCurrentDates();

      // Export button
      this.widgets.exportButton = new YAHOO.widget.Button(this.id + "-export-button", {
        type: "menu",
        menu: this.id + "-export-button-select",
        lazyloadmenu: false
      });
      this.widgets.exportButton.set("disabled", true);

      var onExportMenuItemClick = function (p_sType, p_aArgs) {
        var value = p_aArgs[1].value,
            isDisabled = p_aArgs[1].cfg.getProperty("disabled");
        // Get the function related to the clicked item
        if (!isDisabled && value && (typeof me[value] == "function")) {
          me[value].call(me);
        }
      };
      this._setIdsForYUIMenuAndItems(this.widgets.exportButton);

      this.widgets.exportButton.getMenu().subscribe("click", onExportMenuItemClick);
      Dom.addClass(this.widgets.exportButton.getMenu().element, "export-button");

      // Default listeners
      Event.addListener(this.id + "-home", "click", this.onResetDates, null, this);
      Event.addListener(this.id + "-by-days", "click", this.onChangeDateFilter, { filter: "days" }, this);
      Event.addListener(this.id + "-by-weeks", "click", this.onChangeDateFilter, { filter: "weeks" }, this);
      Event.addListener(this.id + "-by-months", "click", this.onChangeDateFilter, { filter: "months" }, this);
      Event.addListener(this.id + "-by-years", "click", this.onChangeDateFilter, { filter: "years" }, this);
      Event.addListener(this.id + "-chart-prev", "click", this.onChangeDateInterval, { coef: -1 }, this);
      Event.addListener(this.id + "-chart-next", "click", this.onChangeDateInterval, { coef: 1 }, this);

      // Date filter
      Dom.addClass(this.id + "-by-" + this.options.currentDateFilter, "selected");
      return true;
    },

    onSearch: function Tool_onSearch() {
      // To be overridden
    },

    loadSites: function loadSites() {
      if (this.options.siteId && this.options.siteId != "") {
        Alfresco.util.Ajax.jsonGet({
          url: Alfresco.constants.PROXY_URI + "api/sites/" + encodeURIComponent(this.options.siteId),
          successCallback: {
            fn: function (res) {
              var param = {
                json: [{
                    name: this.options.siteId,
                    title: res.json.title || ""
                  }
                ]
              };
              this.createSiteMenu(param, true);
            },
            scope: this
          },
          failureMessage: this.msg("label.popup.error.list-site")
        });
      } else {
        Alfresco.util.Ajax.jsonGet({
          url: Alfresco.constants.PROXY_URI + "share-stats/site/list-sites?role=SiteManager",
          successCallback: {
            fn: function (res) {
              this.createSiteMenu(res);
            },
            scope: this
          },
          failureMessage: this.msg("label.popup.error.list-site"),
          execScripts: true
        });
      }
    },

    onSiteMenuClick: function Tool_onSiteMenuClick(p_sType, p_aArgs, p_oItem) {
      var sText = p_oItem.cfg.getProperty("text");

      this.widgets.siteButton.value = p_oItem.value;
      this.widgets.siteButton.set("label", sText);
      this.execute();
    },

    createSiteMenu: function Tool_createSiteMenu(res, hideAllSiteEntry) {
      var siteMenuButtons = [],
          allSitesMenuButton = [],
          siteIds = [];

      for (var i=0, ii=res.json.length ; i < ii ; i++) {
        var current_site = res.json[i];
        siteMenuButtons.push({
          text: current_site.title,
          value: current_site.name,
          onclick: {
            fn: this.onSiteMenuClick,
            scope: this
          }
        });

        // We store sites informations (id / title)
        siteIds.push(current_site.name);

        this.sites[current_site.name] = current_site.title;
      }

      if (!hideAllSiteEntry) {
        allSitesMenuButton.push({
          text: this.msg("label.menu.site.all"),
          value: '*',

          onclick: {
            fn: this.onSiteMenuClick,
            scope: this
          }
        });
      }

      var menuButtons = allSitesMenuButton.concat(siteMenuButtons);
      var btOpts = {
        type: "split",
        menu: menuButtons,
        lazyloadmenu: false
      };

      if (menuButtons.length <= 1) {
        btOpts.disabled = true;
      }

      this.widgets.siteButton = new YAHOO.widget.Button(this.id + "-site-criteria", btOpts);

      // First item selection
      this.widgets.siteButton.set("label", menuButtons[0].text);
      this.widgets.siteButton.value = menuButtons[0].value;
      this.widgets.siteButton.set("selectedMenuItem", this.widgets.siteButton.getMenu().getItem(0));
      this._setIdsForYUIMenuAndItems(this.widgets.siteButton);

      this.execute();
    },

    buildTimeStampArray: function Tool_buildTimeStampArray() {
      var next,
          hasNext,
          tsArray = [];

      var to = new Date(this.endDatesArray[this.options.currentDateFilter].getTime());
      var from = new Date(this.endDatesArray[this.options.currentDateFilter].getTime());

      // Creating date intervals from starting month to the ending month INCLUDED
      if (this.options.currentDateFilter == "months") {
        tsArray.push(from.setDate(1));
        next = new Date(from);
        next.setDate(1);
        next.setDate(next.getDate() + 1);

        // "to" date
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
      else if (this.options.currentDateFilter == "weeks") {
        // Beginning of the week
        from.setDate(to.getDate() - (to.getDay() - 1));
        next = new Date(from);
        tsArray.push(from.getTime());

        // "to" date
        to.setMonth(from.getMonth());
        to.setDate(from.getDate() + 7);

        next.setDate(from.getDate() + 1);
        hasNext = (to.getTime() > next.getTime());
        while (hasNext) {
          tsArray.push(next.getTime());
          next.setDate(next.getDate() + 1);
          hasNext = (to.getTime() > next.getTime());
        }

        // Next week?
        tsArray.push(next.getTime());
      }
      else if (this.options.currentDateFilter == "days") {
        // "from" date
        tsArray.push(from.getTime());

        // "end" date
        to.setDate(to.getDate() + 1);

        // next day
        next = new Date(from);
        next.setHours(next.getHours() + 2);

        // We check if we don't exceed the end date, we loop
        hasNext = (to > next);
        while (hasNext) {
          tsArray.push(next.getTime());
          next.setHours(next.getHours() + 2);
          hasNext = (to > next);
        }
        tsArray.push(to.getTime());
      } else if (this.options.currentDateFilter == "years") {
        // Beginning of the year
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

    convertMenuValue: function Tool_convertMenuValue(val) {
      var res = null;
      if (val !== undefined && val !== "") {
        res = val;
      }
      return res;
    },

    onChangeDateFilter: function Tool_OnChangeDateFilter(e, args) {
      if (e) {
        Event.stopEvent(e);
      }

      Dom.removeClass(this.id + "-by-" + this.options.currentDateFilter, "selected");
      Dom.addClass(this.id + "-by-" + args.filter, "selected");
      this.options.currentDateFilter = args.filter;
      this.execute();
    },

    onChangeDateInterval: function Tool_OnChangeDateInterval(e, args) {
      var coef = args.coef,
          dateFilter = this.options.currentDateFilter,
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
        default:
          newDate.setFullYear(this.endDatesArray[dateFilter].getFullYear() + (1 * coef));
          break;
      }

      this.endDatesArray[dateFilter] = newDate;
      this.execute();
    },

    setupCurrentDates : function Tool_setupCurrentDates(){
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
    },

    onResetDates: function Tool_OnResetDates(){
      this.setupCurrentDates();
      this.execute();
    },

    execute: function Tool_execute() {
      this.onSearch();
    },

    // For build correctly the X axis of all charts
    buildBarChartXLabels: function Tool_buildBarChartXLabels(params, currentSizeMin) {
      var labels = [],
          timeType = params.currentFilter,
          slicedDates = params.additionalsParams.tsString.split(","),
          truncateLabels = false,
          i, ii;

      if (currentSizeMin && params.chartDomId) {
        var chartElt = document.getElementById(params.chartDomId);
        if (chartElt && chartElt.clientWidth <= currentSizeMin) {
          truncateLabels = true;
        }
      }

      switch (timeType) {
        case "years":
          for (i = 0, ii = slicedDates.length - 1; i < ii; i++) {
            labels[i] = Alfresco.thirdparty.dateFormat(new Date(parseInt(slicedDates[i], 10)), AtolStatistics.dateFormatMasks.fullMonth); // default: mmmm
            if (truncateLabels) {
              labels[i] = labels[i].substring(0,3);
            }
          }
          break;

        case "months":
          for (i = 0, ii = slicedDates.length - 1; i < ii; i++) {
            labels[i] = Alfresco.thirdparty.dateFormat(new Date(parseInt(slicedDates[i], 10)), AtolStatistics.dateFormatMasks.shortDay); // default: dd/mm
            if (truncateLabels) {
              labels[i] = labels[i].substring(0,2);
            }
          }
          break;

        case "weeks":
          for (i = 0, ii = slicedDates.length - 1; i < ii; i++) {
            labels[i] = Alfresco.thirdparty.dateFormat(new Date(parseInt(slicedDates[i], 10)), AtolStatistics.dateFormatMasks.mediumDay); // default: dddd dd/mm
            if (truncateLabels) {
              labels[i] = labels[i].substring(0,3);
            }
          }
          break;

        case "days":
        default:
          for (i = 0, ii = slicedDates.length - 1; i < ii; i++) {
            var timestamp = parseInt(slicedDates[i], 10),
                h1 = Alfresco.thirdparty.dateFormat(new Date(timestamp), AtolStatistics.dateFormatMasks.shortHour), // default: HH'h'
                h2 = Alfresco.thirdparty.dateFormat(new Date(timestamp + (2 * 60 * 60 * 1000)), AtolStatistics.dateFormatMasks.shortHour); // + 2 hours

            labels[i] = h1 + " - " + h2;
          }
          break;
      }
      return labels;
    },

    // For build date title of user-connection and volumetry charts
    buildDateTitle: function Tool_buildDateTitle(params) {
      var title,
          timeType = params.currentFilter,
          slicedDates = params.additionalsParams.tsString.split(","),
          from = new Date(parseInt(slicedDates[0], 10));

      switch (timeType) {
        case "years":
          title = this.getMessage(timeType, "graph.title.date.", from.getFullYear());
          break;

        case "months":
          var m = Alfresco.thirdparty.dateFormat(from, AtolStatistics.dateFormatMasks.fullMonth);
          title = this.getMessage(timeType, "graph.title.date.", m, from.getFullYear());
          break;

        case "weeks":
          title = this.getMessage(timeType, "graph.title.date.", from.getWeek(), from.getFullYear());
          break;

        case "days":
        default:
          title = this.getMessage(timeType, "graph.title.date.", Alfresco.thirdparty.dateFormat(from, AtolStatistics.dateFormatMasks.shortDate));
          break;
      }

      return title;
    },

    buildTitle: function Tool_buildTitle(params) {
      var title,
          site = params.additionalsParams.site,
          siteTitle = params.additionalsParams.siteTitle || '';

      if (site && site != "*") {
        var opt = '"' + ((siteTitle != "") ? siteTitle : site) + '"';
        title = this.getMessage("site", "graph.title.", opt);
      } else {
        title = this.getMessage("all", "graph.title.");
      }

      title += this.buildDateTitle(params);
      return title;
    },

    exportChartAsImage: function Tool_exportChartAsImage(theChart) { // Export with Canvg library
      var wrapper = document.getElementById(theChart.element.id),
        svg = wrapper.querySelector("svg"),
        svgData = null;

      if (window.XMLSerializer) {
        svgData = (new XMLSerializer()).serializeToString(svg);
      } else if (svg.xml) {
        svgData = svg.xml;
      }

      if (svgData) {
        var mycanvas = document.createElement('canvas');
        canvg(mycanvas, svgData);

        if (YAHOO.env.ua.ie) {
          var myIEWindow = window.open("", "");
          myIEWindow.document.write("<image src='" + mycanvas.toDataURL("image/png") + "'/>");
          myIEWindow.focus();
        } else {
          var a = document.createElement("a"),
            chartTitle = theChart.element.querySelector("text.c3-title");

          // The PNG file take the C3 Chart title when it exists
          if (chartTitle) {
            a.download = chartTitle.innerHTML;
          } else {
            a.download = "chart.png";
          }

          a.href = mycanvas.toDataURL("image/png");
          document.body.appendChild(a); // /!\ For FireFox
          a.click();
        }
      }
    },

    onIMGExport: function Tool_onIMGExport() {
      // Empty because it will be overridden
    },

    _setIdsForYUIMenuAndItems: function Tool__setIdsForYUIMenuAndItemst(btn) {
      if (btn) {
        var menu = btn.getMenu();
        if (menu && menu.element) {
          // change menu id
          var newMenuId = btn.get("id") + "-menu"
          menu.id = newMenuId;
          menu.element.setAttribute("id", newMenuId);

          // Menu items
          var menuItems = menu.getItems();
          if(menuItems && menuItems.length > 0) {
            var currentItems = {};

            for (var i=0, ii=menuItems.length ; i<ii ; i++) {
              var item = menuItems[i];
              if (item) {
                // change current menu item ids
                var newItemId = btn.get("id") + "-menu-item-" + (item.value || i);
                item.id = newItemId;
                item.element.setAttribute("id", newItemId);

                // store current item and his index
                currentItems[i] = item;
              }
            }

            // insert menu items with new ids
            for (var currItem in currentItems) {
              if (currentItems.hasOwnProperty(currItem)) {
                menu.insertItem(currentItems[currItem], currItem);
              }
            }
          }

          menu.render();
        }
      }
    }
  });
})();