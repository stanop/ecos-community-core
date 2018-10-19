(function() {

    var jsonData = jsonUtils.toObject(json),
        nodes = jsonData.nodes,
        actionId = jsonData.actionId,
        params = jsonData.params,
        groupType = jsonData.groupType || "selected",
        query = jsonData.query,
        language = jsonData.language || null,
        journalId = jsonData.journalId,
        actionResults,
        records;

    if (!exists("nodes", nodes) ||
        !exists("attributes", params) ||
        !exists("actionId", actionId)) {
        return;
    }

    var results = [];
    if (groupType == "selected") {

        records = recordsService.toRecords(nodes);
        actionResults = groupActions.execute(records, {
            params: params,
            actionId: actionId
        });
        for (var idx in actionResults) {
            var result = actionResults[idx];
            results.push({
                nodeRef: result.data.toString(),
                status: result.status.key,
                message: result.status.message,
                url: result.status.url
            });
        }
    } else {

        records = recordsService.getIterableRecords({
            sourceId: getRecordsSource(journalId),
            query: query,
            language: language || "criteria"
        });

        actionResults = groupActions.execute(records, {
            params: params,
            async: true,
            actionId: actionId
        });

        if (actionResults && actionResults.length > 0) {
            for (var idx in actionResults) {
                var result = actionResults[idx];
                results.push({
                    nodeRef: result.data.toString(),
                    status: result.status.key,
                    message: result.status.message,
                    url: result.status.url
                });
            }
        } else {
            results.push({
                nodeRef: "",
                status: "OK",
                message: msg.get("group-action.filtered.started.title"),
                url: ""
            })
        }
    }

    model.results = results;

})();

function getRecordsSource(journalId) {
    var journalType = journals.getJournalType(journalId);
    if (journalType) {
        var datasourceId = journalType.getDataSource();
        if (datasourceId) {
            var datasource = services.get(datasourceId);
            if (datasource && datasource.getSourceId) {
                return datasource.getSourceId() || "";
            }
        }
    }
    return "";
}

function exists(name, obj) {
    if(!obj) {
        status.setCode(status.STATUS_BAD_REQUEST, "Argument '" + name + "' is required");
        return false;
    }
    return true;
}