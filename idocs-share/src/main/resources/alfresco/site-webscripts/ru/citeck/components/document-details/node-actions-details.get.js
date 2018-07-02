<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

(function () {

    var result = {}

    result.documentDetails = AlfrescoUtil.getNodeDetails(args.nodeRef, args.site, {
        actions: true
    });

    var connector = remote.connect("alfresco");
    var serverActionsConnection = connector.get("/api/node-action-service/get-actions?nodeRef=" + args.nodeRef);
    if (serverActionsConnection.status == 200) {
        result.serverActions = eval('(' + serverActionsConnection.response + ')');
    } else {
        result.serverActions = [];
    }
    model.result = result;

})();

