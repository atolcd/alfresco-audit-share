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
  var defaultApplyConfig = Alfresco.DNDUpload.prototype._applyConfig;

  Alfresco.DNDUpload.prototype._applyConfig = function DNDUpload__applyConfig() {
    // Call default '_applyConfig' function
    defaultApplyConfig.apply(this, arguments);

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
            auditActionName: "file-added"
          };

          for (var i=0 ; i<success ; i++) {
            var file = objComplete.successful[i];

            // File params
            params.auditObject = file.nodeRef;

            // AJAX call
            AtolStatistics.util.insertAuditRemoteCall(params);
          }
        }
      } catch (e) {}
    }
  };
})();