<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function jsonGet(url) {
    var result = remote.connect("alfresco").get(url);

    try {
        if (result.status == 200) {
            var json = eval('(' + result + ')');
            if (json != null) return json;
        }
    } catch (e) {
        return null;
    }

    return null;
}

function getNodeInfo(nodeRef) {
    var details = jsonGet('/citeck/node?nodeRef=' + nodeRef);
    return details != null && details.props ? details : null;
}

function isPendingUpdate(nodeRef) {
    var isPendingUpdate = jsonGet('/citeck/node/check-pending-update?nodeRef=' + nodeRef);
    return isPendingUpdate != null ? isPendingUpdate : false;
}

(function() {

    AlfrescoUtil.param('nodeRef');
    AlfrescoUtil.param('site', null);

    var pendingUpdate = isPendingUpdate(model.nodeRef);

    var status = "";

    if (!pendingUpdate) {
        try {
            var nodeDetails = getNodeInfo(model.nodeRef),
                caseStatus = nodeDetails.assocs ? nodeDetails.assocs["icase:caseStatusAssoc"] : null;

            if (caseStatus && caseStatus[0]) {
                var statusDetails = getNodeInfo(caseStatus[0]);
                status = statusDetails.props["cm:title"] || statusDetails.props["cm:name"];
            }
        } catch(e) {
            status = "";
        }
    }

    model.status = status;
    model.isPendingUpdate = pendingUpdate;

})();