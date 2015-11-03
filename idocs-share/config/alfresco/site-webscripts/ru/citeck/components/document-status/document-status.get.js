<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

(function() {

    AlfrescoUtil.param('nodeRef');
    AlfrescoUtil.param('site', null);
    
    var status = "";
    try {
        var documentDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site);
        status = documentDetails.item.node.properties['idocs:documentStatus'] || "";
    } catch(e) {
        status = "";
    }
    
    if(status) {
        // try to localize status
        var alfresco = remote.connect('alfresco');
        var response = alfresco.call('/citeck/message?key=listconstraint.idocs_constraint_documentStatus.' + status);
        
        if(response.status == 200) {
            status = "" + response;
        }
    }
    
    model.status = status;

})();