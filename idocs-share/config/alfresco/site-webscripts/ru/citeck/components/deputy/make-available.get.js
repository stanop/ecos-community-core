<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

(function(){

    var url = "/api/availability/make-available";
    var argAvailable = args.available;
    if(argAvailable && argAvailable != "") {
        url += "?available=" + argAvailable;
        argAvailable = (argAvailable == true);
    }
    var connector = remote.connect("alfresco");
    var result = connector.post(url, '{}', 'application/json');
    if (result.status != 200){
        AlfrescoUtil.error(result.status, 'Could not set available for current user: request url = ' + url);
    }
    var available = result != "" ? eval('(' + result + ')') : "";
    user.properties.available = available;
    user.save();

    if (argAvailable && argAvailable != available) {
        AlfrescoUtil.error(result.status, 'Available value is not right: request url = ' + url);
    }

})();