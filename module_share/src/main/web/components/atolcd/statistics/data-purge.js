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
 * DataPurge tool component.
 *
 * @namespace AtolStatistics
 * @class AtolStatistics.DataPurge
 */
(function () {
  /**
   * YUI Library aliases
   */
  var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event;

  /**
   * DataPurge constructor.
   *
   * @param {String} htmlId The HTML id of the parent element
   * @return {AtolStatistics.DataPurge} The new DataPurge instance
   * @constructor
   */
  AtolStatistics.DataPurge = function DataPurge_constructor(htmlId) {
    AtolStatistics.DataPurge.superclass.constructor.call(this, "AtolStatistics.DataPurge", htmlId, ["button", "container", "json"]);
    this.options.purgeAuthorized = false;

    return this;
  };

  YAHOO.extend(AtolStatistics.DataPurge, AtolStatistics.Tool, {
    /**
     * Fired by YUI when parent element is available for scripting.
     * Component initialisation, including instantiation of YUI widgets and event listener binding.
     *
     * @method onReady
     */

    onReady: function DataPurge_onReady() {
      AtolStatistics.DataPurge.superclass.onReady.call(this);

      var me = this;

      // Event on purge button
      Event.addListener(this.id + "-purge-button", "click", this.onPurgeButtonClick, this, true);

      // Event on repository checkbox
      Event.addListener(this.id + "-repository_purge-criteria", "click", this.execute, null, this);

      // Event on all event checkbox
      Event.addListener(this.id + "-purge_all-criteria", "click", this.execute, null, this);

      this.widgets.tableCriteriaButton = new YAHOO.widget.Button(this.id + "-table-criteria", {
        type: "split",
        menu: this.id + "-table-criteria-select",
        lazyloadmenu: false
      });
      this.widgets.tableCriteriaButton.getMenu().cfg.setProperty("zIndex", 4);
      this.widgets.tableCriteriaButton.value = "audit_entry";

      // hide the "export" button (For the moment)
      this.widgets.exportButton.addClass("hidden");

      // Listeners on menu click
      // "Purge tables" filter
      var onTablesMenuItemClick = function (p_sType, p_aArgs, p_oItem) {
        var sText = p_aArgs[1].cfg.getProperty("text"),
            value = p_aArgs[1].value;

        me.widgets.tableCriteriaButton.value = value;

        // Repository purge is only for audit_entry table
        (me.widgets.tableCriteriaButton.value != 'audit_entry') ? document.getElementById(me.id + "-repository_purge-criteria").disabled = true :
          document.getElementById(me.id + "-repository_purge-criteria").disabled = false;

        me.widgets.tableCriteriaButton.set("label", sText);
        me.execute();
      };
      this.widgets.tableCriteriaButton.getMenu().subscribe("click", onTablesMenuItemClick);

      // Add separator before last item
      var itemsCount = this.widgets.tableCriteriaButton.getMenu().getItems().length;
      if (itemsCount > 0) {
        Dom.addClass(this.widgets.tableCriteriaButton.getMenu().getItem(itemsCount - 1).element, "menu-separator");
      }

      this.loadSites();
    },

    onPurgeButtonClick: function DataPurge_onPurgeButtonClick() {

      var me = this;
      // Ensure that a period is selected
      if (!Dom.get(this.id + "-purge_all-criteria").checked && (document.getElementById(this.id + "-period-from").value == '' || document.getElementById(this.id + "-period-to").value == '')) {
        var body = '<div class="node-details-popup">';
        body += '<p><label>' + me.getMessage("label.popup.period_empty") + '</label></p>';
        body += '</div>';

        Alfresco.util.PopupManager.displayPrompt({
          title: this.getMessage("label.popup.empty_title"),
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

      var body = '<div class="node-details-popup">';
      body += '<p><label>' + me.getMessage("label.popup.body") + '</label></p>';
      body += '</div>';

      Alfresco.util.PopupManager.displayPrompt({
        title: me.getMessage("label.popup.title"),
        text: body,
        close: true,
        noEscape: true,
        buttons: [{
          text: me.getMessage("label.popup.yes"),
          handler:{
            fn: function() {
              me.options.purgeAuthorized = true;
              me.execute();
              this.destroy();
            }
          }
        }, {
          text: me.getMessage("label.popup.no"),
          handler: function () {
            this.destroy();
          },
          isDefault: true
        }]
      });
    },

    getMessage: function DataPurge_getMessage(messageId, prefix) {
      var msg = (prefix) ? prefix + messageId : messageId;
      var res = Alfresco.util.message.call(null, msg, "AtolStatistics.DataPurge", Array.prototype.slice.call(arguments).slice(2));
      res = (res.search("graph.label") == 0) ? messageId : res;
      return res;
    },

    convertDateToTimeStamp: function DataPurge_convertDateToTimeStamp(dateString) {
      var dateTimeParts = dateString.split(' '),
          dateParts = dateTimeParts[0].split('/');

      return new Date(dateParts[2], parseInt(dateParts[1], 10) - 1, dateParts[0]).getTime();
    },

    onSearch: function DataPurge_onSearch() {
      // When the repository checkbox is checked
      if (Dom.get(this.id + "-repository_purge-criteria").checked) {
        this.widgets.tableCriteriaButton.set("disabled", true);
        this.widgets.siteButton.set("disabled", true);
        this.widgets.siteButton.value = "_repository";
      } else {
        this.widgets.tableCriteriaButton.set("disabled", false);
        this.widgets.siteButton.set("disabled", false);
      }

      // When All periods checkbox is checked
      if (Dom.get(this.id + "-purge_all-criteria").checked) {
        document.getElementById(this.id + "-period-from").disabled = true;
        document.getElementById(this.id + "-period-to").disabled = true;
        document.getElementById(this.id + "-period-from").value = '';
        document.getElementById(this.id + "-period-to").value = '';
      } else {
        document.getElementById(this.id + "-period-from").disabled = false;
        document.getElementById(this.id + "-period-to").disabled = false;
      }

      // Retrieve variables from interface
      if (this.options.purgeAuthorized) {
        var purgeTable = this.convertMenuValue(this.widgets.tableCriteriaButton.value),
            from = this.convertDateToTimeStamp(document.getElementById(this.id + "-period-from").value),
            to = this.convertDateToTimeStamp(document.getElementById(this.id + "-period-to").value),
            site = this.convertMenuValue(this.widgets.siteButton.value);

        // Build query parameters
        this.lastRequest.params = this.buildParams(purgeTable, from, to, site);

        var url = Alfresco.constants.PROXY_URI + "share-stats/delete-audits" + this.lastRequest.params;
        Alfresco.util.Ajax.jsonDelete({
          url: url,
          successCallback: {
            fn: this.tableDataPurge,
            scope: this
          },
          failureMessage: this.msg("label.popup.query.error"),
          execScripts: true,
          additionalsParams: {
            purgeTable: purgeTable,
            from: from,
            to: to,
            site: site
          }
        });
        this.options.purgeAuthorized = false;
      }
    },

    tableDataPurge: function DataPurge_tableDataPurge(response) {
      if (response.json) {
        var me = this;
        var body = '<div class="node-details-popup">';
        body += '<p><label>' + me.getMessage("label.popup.finish") + '</label></p>';
        body += '</div>';

        Alfresco.util.PopupManager.displayPrompt({
          title: this.getMessage("label.popup.success"),
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
      }
    },

    /**
     * @method buildParams
     *         This function is used to build DELETE query request
     *
     * @param from Timestamp (string) - date from
     * @param to Timestamp (string) - date to
     * @param purgeTable - selected purgeTable
     * @param site - selected site

     * @return string - url params
     */
    buildParams: function DataPurge_buildParams(purgeTable, from, to, site) {
      var params = "";

      if (purgeTable !== null) {
        if (purgeTable === "all") {
          var purgeTableValues = [],
              items = this.widgets.tableCriteriaButton.getMenu().getItems();
          for (var i=0, ii=items.length ; i<ii ; i++) {
            var item = items[i];
            if (item.value != "") {
              purgeTableValues.push(item.value);
            }
          }

          params += "?purgeTables=" + purgeTableValues.join(',') + "&combined=true";
        } else {
          params += "?purgeTable=" + purgeTable;
        }
      }
      if (site !== null) {
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
      if (from) {
        params += "&from=" + from;
      }
      if (to) {
        params += "&to=" + to;
      }
      return params;
    },

    execute: function DataPurge_execute() {
      AtolStatistics.DataPurge.superclass.execute.call(this);
    }
  });
})();