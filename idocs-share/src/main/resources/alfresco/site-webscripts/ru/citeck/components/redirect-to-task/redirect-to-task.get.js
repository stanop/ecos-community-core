<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

(function() {
    AlfrescoUtil.param('nodeRef');
    AlfrescoUtil.param('site', null);

    var taskNode = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site),
        taskId = taskNode.item.node.properties["cm:name"],
        taskStatus = taskNode.item.node.properties["bpm:status"],
        taskDocument = taskNode.item.node.properties["wfm:document"],
        isCardDetail = taskStatus != "Completed" && taskDocument,
        redirectURL = "/share/page/";

    if (isCardDetail) {
        redirectURL += "card-details?nodeRef=" + taskDocument;
    } else {
        var result = remote.call("citeck/invariants/view-check?taskId=" + taskNode.item.node.properties["cm:name"]),
            formMode = taskStatus == "Completed" ? "view" : "edit",
            formId = result.exists ? "task-view-edit" : (formMode == "edit" ? "task-edit" : "task-details"),
            requestParams = "?taskId=" + taskId + "&formMode=" + formMode;

        redirectURL += formId + requestParams;
    }

    status.code = 303;
    status.location = redirectURL;
})();
