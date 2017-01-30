const STATUS_SUCCESS = "OK";
const STATUS_ERROR = "ERROR";

(function() {

    var nodes = json.get('nodes'),
        attrs = json.get('attributes');

    if (!exists("nodes", nodes) ||
        !exists("attributes", attrs)) {

        return;
    }

    var results = {};
    for (var i = 0; i < nodes.length(); i++) {
        var node = search.findNode(nodes.get(i));
        var changeResult  = {};
        var it = attrs.keys();
        while (it.hasNext()) {
            var key = it.next();
            try {
                attributes.set(node, key, attrs.get(key));
                changeResult[key] = STATUS_SUCCESS;
            } catch (e) {
                changeResult[key] = STATUS_ERROR;
            }
        }
        throw "Errror!!!!";
        results[node.nodeRef] = changeResult;
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