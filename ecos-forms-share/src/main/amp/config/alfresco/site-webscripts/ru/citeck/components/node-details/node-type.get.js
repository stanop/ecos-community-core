<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

(function(){
    var nodeRef = AlfrescoUtil.param("nodeRef");
    if(!nodeRef) {
        AlfrescoUtil.error('There are not nodeRef. nodeRef=' + nodeRef);
    }
    nodeRef = nodeRef.replace("://", "/");
    var url = "/slingshot/doclib2/node/" + nodeRef;
    var result = remote.call(url);
    if (result.status != 200){
        AlfrescoUtil.error(result.status, 'Could not load node type: request url = ' + url);
    }
    model.nodeType = eval('(' + result + ')').item.node.type;
})();