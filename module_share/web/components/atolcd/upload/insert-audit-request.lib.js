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

// AtolStatistics top-level util namespace.
AtolStatistics.util = AtolStatistics.util || {};

// AtolStatistics top-level constants namespace.
AtolStatistics.constants = AtolStatistics.constants || {};

AtolStatistics.constants.SITE_REPOSITORY = "_repository";


(function() {
  AtolStatistics.util.insertAuditRemoteCall = function AtolStatistics_insertAuditRemoteCall(params) {
    // AJAX call
    // Request body example:
    /*
    {
      "id": "0",
      "auditTime": "1368715747969",
      "auditObject": "",
      "auditSite": "_repository",
      "auditUserId": "admin",
      "auditAppName": "document",
      "auditActionName": "file-added"
    }
    */

    params.auditUserId = Alfresco.constants.USERNAME;
    params.auditTime = new Date().getTime();

    Alfresco.util.Ajax.request({
      method: Alfresco.util.Ajax.POST,
      url: Alfresco.constants.PROXY_URI + "share-stats/insert-audit",
      requestContentType: "text/plain;charset=UTF-8",
      dataStr: YAHOO.lang.JSON.stringify(params),
      successCallback: {
        fn: function(res, obj) {
          if (Alfresco.logger.isDebugEnabled()){
            Alfresco.logger.debug("[Share-stats] Audit correctly inserted for file: " + obj.auditObject || '');
          }
        },
        scope: this,
        obj: params
      },
      failureCallback: {
        fn: function(res, obj) {
          if (Alfresco.logger.isDebugEnabled()){
            Alfresco.logger.debug("[Share-stats] Audit insert has failed for file: " + obj.auditObject || '');
            Alfresco.logger.debug(res.serverResponse.responseText || '');
          }
        },
        scope: this,
        obj: params
      }
    });
  };
})();