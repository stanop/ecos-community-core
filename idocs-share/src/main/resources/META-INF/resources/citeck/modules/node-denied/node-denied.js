(function() {

    var checkPermissions = function() {
        var nodeRef = /nodeRef=([^&]+)/.exec(window.location.href)[1];
        if (!nodeRef) return;
        Alfresco.util.Ajax.jsonGet({
            url: Alfresco.constants.PROXY_URI + 'citeck/has-permission?nodeRef=' + nodeRef + '&permission=Read',
            successCallback: {
                fn: function(response) {
                    if (response.json == false) {
                        window.location.href = "/share";
                    }
                }
            },
            failureCallback: {
                fn: function(response) {
                    console.error(response.json);
                }
            }
        });
    };

    checkPermissions();
    YAHOO.Bubbling.on("metadataRefresh", checkPermissions, this);

})();