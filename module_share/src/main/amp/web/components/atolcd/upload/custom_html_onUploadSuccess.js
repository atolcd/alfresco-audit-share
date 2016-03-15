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

(function() {
  if (Alfresco.HtmlUpload) {
    var defaultOnUploadSuccess = Alfresco.HtmlUpload.prototype.onUploadSuccess;

    Alfresco.HtmlUpload.prototype.onUploadSuccess = function HtmlUpload_onUploadSuccess(response) {
      if (defaultOnUploadSuccess) {
        // Call default 'onUploadSuccess' function
        defaultOnUploadSuccess.apply(this, arguments);
      }

      // Only on repository files (we use doclib activities in sites)
      if (!this.widgets.siteId.value) {
        try {
          var params = {
            id: "0",
            auditSite: AtolStatistics.constants.SITE_REPOSITORY,
            auditAppName: "document",
            auditActionName: "file-added",
            auditObject: response.nodeRef
          };

          var getSiteSuccessHandler = function(res, obj) {
            if (res.json.siteShortName) {
              // Finally, we are on a site
              obj.auditSite = res.json.siteShortName;
            }

            // Only way to know if this is a file update
            if (typeof res.json.isFileCreation != "undefined" && res.json.isFileCreation == false) {
              obj.auditActionName = "file-updated";
            }

            // Insert audit (AJAX call)
            AtolStatistics.util.insertAuditRemoteCall(obj);
          }

          // Verify if we are into a site (/Company Home/Sites/{siteShortName}/documentLibrary/...)
          Alfresco.util.Ajax.jsonGet({
            url: Alfresco.constants.PROXY_URI + "share-stats/get-site/node/" + params.auditObject.replace('://', '/') + "?checkDates=true",
            successCallback: {
              fn: getSiteSuccessHandler,
              scope: this,
              obj: params
            }
          });
        } catch (e) {}
      }
    };
  }
})();