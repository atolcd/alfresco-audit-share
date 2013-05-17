(function() {
  var defaultOnUploadCompleteData = Alfresco.FlashUpload.prototype.onUploadCompleteData;

  Alfresco.FlashUpload.prototype.onUploadCompleteData = function FlashUpload_onUploadCompleteData(event) {
    // Call default 'onUploadCompleteData' function
    defaultOnUploadCompleteData.apply(this, arguments);

    // Only on repository file
    if (!this.showConfig.siteId) {
      try {
        var params = {
          auditSite: "_repository",
          auditAppName: "document",
          auditActionName: "file-added",
          auditTime: new Date().getTime(),
          auditUserId: Alfresco.constants.USERNAME
        };

        // File id
        var id = event.id.split("file").reverse()[0];
        if (isNaN(id)) { id = 0; }
        params.id = id;

        var json = Alfresco.util.parseJSON(event.data);
        if (json) {
          // Add nodeRef
          params.auditObject = json.nodeRef;
        }

        // AJAX call
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
      } catch (e) {}
    }
  };
})();