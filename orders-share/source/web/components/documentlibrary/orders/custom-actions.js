(function() {

    YAHOO.Bubbling.fire("registerAction", {
        actionName: "onActionDocumentEditInternal",
        fn: function(record) {
            var page = "/share/page/edit-metadata?nodeRef=" + record.jsNode.nodeRef + "&formId=";
            if(checkBelongsUserToGroup(Alfresco.constants.USERNAME, "GROUP_SD")) {
                page += "sd";
            }
            if(checkBelongsUserToGroup(Alfresco.constants.USERNAME, "GROUP_registrars")
                || checkBelongsUserToGroup(Alfresco.constants.USERNAME, "GROUP_clerks")) {
                page += "-registrator";
            }
            window.open(page, '_self', false);
        }
    });

    function checkBelongsUserToGroup(user, group) {
        var url = "" + Alfresco.constants.PROXY_URI + "acm/checkBelongsUserToGroup/" + user + "/" + group;
        var xml = new XMLHttpRequest();
        xml.open("GET", url, false);
        xml.send(null);
        if (xml.status != 200) {
            return false;
        }
        return eval("(" + xml.responseText + ")").data;
    }

})();
