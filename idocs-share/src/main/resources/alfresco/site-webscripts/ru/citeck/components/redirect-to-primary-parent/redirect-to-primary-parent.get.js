<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

(function () {
    AlfrescoUtil.param('nodeRef');

    var url = "/citeck/node/parent-node-ref?child=" + model.nodeRef;
    var result = remote.call(url);
    if (result.status != 200) {
        AlfrescoUtil.error(result.status, 'Could not redirect to parent node, because cant get parent node from nodeRef: '
            + model.nodeRef);
    }

    var response = JSON.parse(result),
        parentRef = response && response.data ? response.data : '',
        redirectURL = parentRef ? '/share/page/card-details?nodeRef=' + parentRef : '';

    status.code = 303;
    status.location = redirectURL;
})();
