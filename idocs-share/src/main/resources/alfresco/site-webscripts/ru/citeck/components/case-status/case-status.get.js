<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

const STATUS_TYPE_CASE = 'case-status';
const STATUS_TYPE_DOCUMENT = 'document-status';

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

function getDocumentStatusLocalization(status) {

    var result = status;

    var url = '/citeck/message?key=listconstraint.idocs_constraint_documentStatus.' + status;
    var response = remote.connect('alfresco').call(url);

    if (response.status == 200) {
        result = "" + response;
    }
    return result;
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

    var status = "",
        statusType = STATUS_TYPE_CASE;

    if (!pendingUpdate) {
        try {
            var nodeDetails = getNodeInfo(model.nodeRef),
                caseStatus = nodeDetails.assocs ? nodeDetails.assocs["icase:caseStatusAssoc"] : null,
                documentStatus = nodeDetails.props ? nodeDetails.props["idocs:documentStatus"] : null;

            if (caseStatus && caseStatus[0]) {
                var statusDetails = getNodeInfo(caseStatus[0]);
                status = statusDetails.props["cm:title"] || statusDetails.props["cm:name"];
            } else if (documentStatus) {
                status = getDocumentStatusLocalization(documentStatus);
                statusType = STATUS_TYPE_DOCUMENT;
            }
        } catch(e) {
            status = "";
        }
    }

    model.status = status;
    model.isPendingUpdate = pendingUpdate;
    model.statusType = statusType;

})();