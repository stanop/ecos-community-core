<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

(function() {

    AlfrescoUtil.param('nodeRef');
    AlfrescoUtil.param('site', null);
    model.nodeRefExists = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site);

})();