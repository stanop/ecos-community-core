<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

(function() {

    var PARENT_REF = "workspace://SpaceStore/absenceEvents";

    var url = "/api/availability/absence-event?user=" + user.id;
    var connector = remote.connect("alfresco");
    var result = connector.call(url);
    if (result.status != 200){
        AlfrescoUtil.error(result.status, 'Could not load absence event for user: request url = ' + url);
    }

    var nodeRef = result != "" ? eval('(' + result + ')') : "";

    var targetUrl = "";
    if(nodeRef && nodeRef != "") {
        targetUrl = "/share/page/edit-metadata?nodeRef=" + nodeRef;
    } else {
        targetUrl = "/share/page/create-content?destination=" + PARENT_REF + "&itemId=deputy:absenceEvent" ;
    }

    model.targetUrl = targetUrl;
})();