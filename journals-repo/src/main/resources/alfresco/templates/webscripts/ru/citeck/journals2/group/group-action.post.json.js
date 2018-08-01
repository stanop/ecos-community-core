(function() {

    var jsonData = jsonUtils.toObject(json),
        nodes = jsonData.nodes,
        actionId = jsonData.actionId,
        params = jsonData.params,
        groupType = jsonData.groupType || "selected",
        query = jsonData.query,
        language = jsonData.language || null,
        journalId = jsonData.journalId;

    if (!exists("nodes", nodes) ||
        !exists("attributes", params) ||
        !exists("actionId", actionId)) {
        return;
    }

    var results = [];
    if (groupType == "selected") {
        var actionResults = groupActionService.execute(nodes, actionId, { params: params });
        for (var idx in actionResults) {
            var result = actionResults[idx];
            results.push({
                nodeRef: result.remoteRef.toString(),
                status: result.status.key,
                message: result.status.message,
                url: result.status.url
            });
        }
    } else {
        var records = journals.getRecordsLazy(journalId, query, language, null);
        groupActionService.execute(records, actionId, { params: params, async: true});

        results.push({
            nodeRef: "",
            status: "OK",
            message: msg.get("group-action.filtered.started.title"),
            url: ""
        })
    }

    model.results = results;

})();

function exists(name, obj) {
    if(!obj) {
        status.setCode(status.STATUS_BAD_REQUEST, "Argument '" + name + "' is required");
        return false;
    }
    return true;
}