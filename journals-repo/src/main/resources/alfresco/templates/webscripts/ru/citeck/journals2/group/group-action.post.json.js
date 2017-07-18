(function() {

    var nodes = json.get('nodes'),
        actionId = json.get('actionId'),
        params = json.get('params');

    if (!exists("nodes", nodes) ||
        !exists("attributes", params) ||
        !exists("actionId", actionId)) {

        return;
    }

    var statusByNode = groupActionService.invoke(nodes, actionId, params);

    var results = [];

    for (var node in statusByNode) {
        var status = statusByNode[node];
        results.push({
            nodeRef: node.nodeRef,
            status: status.getStatus(),
            message: status.getMessage()
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