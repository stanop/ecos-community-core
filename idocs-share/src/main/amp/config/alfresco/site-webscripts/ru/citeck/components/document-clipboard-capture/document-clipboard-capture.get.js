<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

(function() {

    AlfrescoUtil.param('nodeRef');
    AlfrescoUtil.param('documentProperty');
    AlfrescoUtil.param('site', null);

    var property = "";
    try {
        var documentDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site);
        property = documentDetails.item.node.properties[model.documentProperty] || "";
    } catch(e) {
        property = "";
    }

    model.property = property;
})();