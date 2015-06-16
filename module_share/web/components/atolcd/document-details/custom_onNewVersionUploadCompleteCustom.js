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
  if (Alfresco.DocumentActions) {
    var defaultOnNewVersionUploadCompleteCustom = Alfresco.DocumentActions.prototype.onNewVersionUploadCompleteCustom;

    Alfresco.DocumentActions.prototype.onNewVersionUploadCompleteCustom = function DocumentActions_onNewVersionUploadCompleteCustom(complete) {
      try {
        if (this.fileUpload && this.fileUpload.uploader && this.fileUpload.uploader.hide) {
          this.fileUpload.hide();
        }

        var params = {
          id: "0",
          auditSite: (this.options.siteId) ? this.options.siteId : AtolStatistics.constants.SITE_REPOSITORY,
          auditAppName: "document",
          auditActionName: "file-updated",
          auditObject: complete.successful[0].nodeRef
        };

        if (!this.options.siteId) {
          var getSiteSuccessHandler = function(res, args) {
            if (res.json.siteShortName) {
              // Finally, we are on a site
              args.params.auditSite = res.json.siteShortName;
            }

            // Insert audit (AJAX call)
            // Call default 'onNewVersionUploadCompleteCustom' function
            AtolStatistics.util.insertAuditRemoteCall(args.params, {
              defaultCallback: args.defaultCallback,
              arguments: args.arguments,
              scope: this
            });
          }

          // Verify if we are into a site (/Company Home/Sites/{siteShortName}/documentLibrary/...)
          Alfresco.util.Ajax.jsonGet({
            url: Alfresco.constants.PROXY_URI + "share-stats/get-site/node/" + params.auditObject.replace('://', '/'),
            successCallback: {
              fn: getSiteSuccessHandler,
              scope: this,
              obj: {
                params: params,
                defaultCallback: defaultOnNewVersionUploadCompleteCustom,
                arguments: arguments
              }
            },
            failureCallback: {
              fn: function(res, args) {
                // Call default 'onNewVersionUploadCompleteCustom' function
                args.defaultCallback.apply(this, args.arguments);
              },
              scope: this,
              obj: {
                defaultCallback: defaultOnNewVersionUploadCompleteCustom,
                arguments: arguments
              }
            }
          });
        } else {
          // Insert audit (AJAX call)
          // Call default 'onNewVersionUploadCompleteCustom' function
          AtolStatistics.util.insertAuditRemoteCall(params, {
            defaultCallback: defaultOnNewVersionUploadCompleteCustom,
            arguments: arguments,
            scope: this
          });
        }
      } catch (e) {
        // Call default 'onNewVersionUploadCompleteCustom' function
        defaultOnNewVersionUploadCompleteCustom.apply(this, arguments);
      }
    };
  }
})();