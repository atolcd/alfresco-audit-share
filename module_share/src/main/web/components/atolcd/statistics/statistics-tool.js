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
  'BYTES_KB': 1024,
  'BYTES_MB': 1048576,
  'BYTES_GB': 1073741824,
  'BYTES_TB': 1099511627776
};

AtolStatistics.util.roundNumber = function (number, digits) {
  var multiple = Math.pow(10, digits);
  var rndedNum = Math.round(number * multiple) / multiple;
  return rndedNum;
}

AtolStatistics.util.formatFileSize = function (fileSize) {
  if (typeof fileSize == "string") {
    fileSize = parseInt(fileSize, 10);
  }

  if (fileSize < AtolStatistics.util.fileSizes.BYTES_KB) {
    return  {
      unitValue: 1,
      value: fileSize,
      message: Alfresco.util.message("size.bytes")
    };
  }
  else if (fileSize < AtolStatistics.util.fileSizes.BYTES_MB) {
    return  {
      unitValue: AtolStatistics.util.fileSizes.BYTES_KB,
      value: AtolStatistics.util.roundNumber(fileSize / AtolStatistics.util.fileSizes.BYTES_KB, 2),
      message: Alfresco.util.message("size.kilobytes")
    };
  }
  else if (fileSize < AtolStatistics.util.fileSizes.BYTES_GB) {
    return  {
      unitValue: AtolStatistics.util.fileSizes.BYTES_MB,
      value: AtolStatistics.util.roundNumber(fileSize / AtolStatistics.util.fileSizes.BYTES_MB, 2),
      message: Alfresco.util.message("size.megabytes")
    };
  }
  else if (fileSize < AtolStatistics.util.fileSizes.BYTES_TB) {
    return  {
      unitValue: AtolStatistics.util.fileSizes.BYTES_GB,
      value: AtolStatistics.util.roundNumber(fileSize / AtolStatistics.util.fileSizes.BYTES_GB, 2),
      message: Alfresco.util.message("size.gigabytes")
    };
  }

  return  {
    unitValue: AtolStatistics.util.fileSizes.BYTES_TB,
    value: AtolStatistics.util.roundNumber(fileSize / AtolStatistics.util.fileSizes.BYTES_TB, 2),
    message: Alfresco.util.message("size.terabytes")
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
      var me = this;
      this.setupCurrentDates();

      // Export button
      this.widgets.exportButton = new YAHOO.widget.Button(this.id + "-export-button", {
        type: "menu",
        menu: this.id + "-export-button-select",
        lazyloadmenu: false
      });
      this.widgets.exportButton.set("disabled", true);

      var onExportMenuItemClick = function (p_sType, p_aArgs, p_oItem) {
        var value = p_aArgs[1].value,
            isDisabled = p_aArgs[1].cfg.getProperty("disabled");
        // Get the function related to the clicked item
        if (!isDisabled && value && (typeof me[value] == "function")) {
          me[value].call(me);
        }
      };
      this.widgets.exportButton.getMenu().subscribe("click", onExportMenuItemClick);
      Dom.addClass(this.widgets.exportButton.getMenu().element, "export-button");

      // Disable "export as image" item for IE8 and under
      if (YAHOO.env.ua.ie && ((!document.documentMode && YAHOO.env.ua.ie < 9) || document.documentMode < 9)) {
        var items = this.widgets.exportButton.getMenu().getItems();
        for (var i=0, ii=items.length ; i<ii ; i++) {
          if (items[i].value == "onIMGExport") {
            items[i].cfg.setProperty("disabled", true);
            break;
          }
        }
      }

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
    },

    onSearch: function Tool_onSearch() {
      // To be overridden
    },

    loadSites: function loadSites() {
      // Loading icon
      // this.widgets.siteButton.set("label", this.msg("label.sites.loading") + ' <span class="loading"></span>');

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
          menuButtons = [],
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
          value: siteIds.join(','),

          onclick: {
            fn: this.onSiteMenuClick,
            scope: this
          }
        });
      }

      menuButtons = allSitesMenuButton.concat(siteMenuButtons);
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

      this.execute();
    },

    buildTimeStampArray: function Tool_buildTimeStampArray() {
      var tsArray = [],
        from = null,
        to = null,
        currentDay = null,
        next = null,
        hasNext = null,
        res = "";

      to = new Date(this.endDatesArray[this.options.currentDateFilter].getTime());
      from = new Date(this.endDatesArray[this.options.currentDateFilter].getTime());

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
        next = null, currentDay = to.getDay(), hasNext = false;

        // Beginning of the week
        from.setDate(to.getDate() - (currentDay - 1));
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
      if (e) Event.stopEvent(e);
      Dom.removeClass(this.id + "-by-" + this.options.currentDateFilter, "selected");
      Dom.addClass(this.id + "-by-" + args.filter, "selected");
      this.options.currentDateFilter = args.filter;
      this.execute();
    },

    onChangeDateInterval: function Tool_OnChangeDateInterval(e, args) {
      var coef = args.coef,
          currentDate = new Date(),
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
    }
  });
})();