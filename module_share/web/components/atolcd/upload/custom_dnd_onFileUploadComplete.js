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
  if (Alfresco.DNDUpload) {
    var defaultApplyConfig = Alfresco.DNDUpload.prototype._applyConfig;

    Alfresco.DNDUpload.prototype._applyConfig = function DNDUpload__applyConfig() {
      if (defaultApplyConfig) {
        // Call default '_applyConfig' function
        defaultApplyConfig.apply(this, arguments);
      }

      // onFileUploadComplete callback
      var onFileUploadCompleteCallback = {
        fn: this.onFileUploadComplete,
        scope: this
      };

      // Check if there is already a 'FileUploadCompleteCallback' defined
      if (this.showConfig.onFileUploadComplete) {
        var currentOnFileUploadCompleteCallback = this.showConfig.onFileUploadComplete;
        onFileUploadCompleteCallback.obj = currentOnFileUploadCompleteCallback;
      }

      // Set the new callback
      this.showConfig.onFileUploadComplete = onFileUploadCompleteCallback;
    };

    Alfresco.DNDUpload.prototype.onFileUploadComplete = function DNDUpload__onFileUploadComplete(objComplete, defaultCallback) {
      // If exists, call default 'onFileUploadComplete' first
      if (defaultCallback && typeof defaultCallback.fn == "function") {
         // Call the onFileUploadComplete callback in the correct scope
         defaultCallback.fn.call((typeof defaultCallback.scope == "object" ? defaultCallback.scope : this), objComplete, defaultCallback.obj);
      }

      // Only on repository files (we use doclib activities in sites)
      if (!this.showConfig.siteId) {
        try {
          var success = objComplete.successful.length;
          if (success > 0) {
            var params = {
              id: "0",
              auditSite: AtolStatistics.constants.SITE_REPOSITORY,
              auditAppName: "document",
              auditActionName: "file-added" // it cannot be a "file updated" event with DnD
            };


            var getSiteSuccessHandler = function(res, args) {
              var params = args.params;

              if (res.json.siteShortName) {
                // Finally, we are on a site
                params.auditSite = res.json.siteShortName;
              }

              for (var i=0 ; i<args.success ; i++) {
                // File nodeRef
                params.auditObject = args.objComplete.successful[i].nodeRef;

                // Insert audit (AJAX call)
                AtolStatistics.util.insertAuditRemoteCall(params);
              }
            }

            // We add files into the same folder so we do only one call
            var firstNodeRef = objComplete.successful[0].nodeRef;

            // Verify if we are into a site (/Company Hom/Sites/{siteShortName}/documentLibrary/...)
            Alfresco.util.Ajax.jsonGet({
              url: Alfresco.constants.PROXY_URI + "share-stats/get-site/node/" + firstNodeRef.replace('://', '/'),
              successCallback: {
                fn: getSiteSuccessHandler,
                scope: this,
                obj: {
                  params: params,
                  success: success,
                  objComplete: objComplete
                }
              }
            });
          }
        } catch (e) {}
      }
    };
  }
})();