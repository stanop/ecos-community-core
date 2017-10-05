(function() {

    var jsonData = jsonUtils.toObject(json),
        nodes = jsonData.nodes,
        actionId = jsonData.actionId,
        params = jsonData.params;

    if (!exists("nodes", nodes) ||
        !exists("attributes", params) ||
        !exists("actionId", actionId)) {
        return;
    }

    var statuses = groupActionService.invoke(nodes, actionId, params);

    var results = [];

    var statusNodes = statuses.getNodes();

    for (var idx in statusNodes) {
        var node = statusNodes[idx];
        results.push({
            nodeRef: node.nodeRef.toString(),
            status: statuses.getStatus(node),
            message: statuses.getMessage(node),
            url: statuses.getUrl(node)
        });
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