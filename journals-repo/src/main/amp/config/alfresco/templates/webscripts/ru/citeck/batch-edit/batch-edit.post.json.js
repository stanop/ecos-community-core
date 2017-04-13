const STATUS_SUCCESS = "OK";
const STATUS_ERROR = "ERROR";
const STATUS_PERMISSION_DENIED = "PERMISSION_DENIED";

(function() {

    var nodes = json.get('nodes'),
        attrs = json.get('attributes'),
        permService = services.get("attributesPermissionService");

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
                if (checkPermission(node, key, permService)) {
                    attributes.set(node, key, attrs.get(key));
                    changeResult[key] = STATUS_SUCCESS;
                } else {
                    changeResult[key] = STATUS_PERMISSION_DENIED;
                }
            } catch (e) {
                logger.error(e);
                changeResult[key] = STATUS_ERROR;
            }
        }
        results[node.nodeRef] = changeResult;
    }

    model.results = results;
})();

function checkPermission(node, attr, permService) {
    return permService.isFieldEditable(citeckUtils.createQName(attr), node.nodeRef, "edit");
}

function exists(name, obj) {
    if(!obj) {
        status.setCode(status.STATUS_BAD_REQUEST, "Argument '" + name + "' is required");
        return false;
    }
    return true;
}