const STATUS_SUCCESS = "OK";
const STATUS_ERROR = "ERROR";
const STATUS_PERMISSION_DENIED = "PERMISSION_DENIED";
const STATUS_SKIPPED = "STATUS_SKIPPED";

(function() {

    var nodes = json.get('nodes'),
        attrs = json.get('attributes'),
        skipInStatuses = json.get('skipInStatuses'),
        permService = services.get("attributesPermissionService");

    if (!exists("nodes", nodes) ||
        !exists("attributes", attrs)) {

        return;
    }

    var results = {};
    var statuses = [];
    if (skipInStatuses) {
        for (var i = 0; i < skipInStatuses.length(); i++) {
            var statusName = skipInStatuses.get(i).trim();
            var status = caseStatusService.getStatusByName(statusName);
            if (status != null) {
                statuses.push(status.getNodeRef());
            }
        }
    }
    statuses = statuses.join(",");

    for (var i = 0; i < nodes.length(); i++) {
        var node = search.findNode(nodes.get(i));
        var changeResult  = {};
        var it = attrs.keys();
        while (it.hasNext()) {
            var key = it.next();
            var skipInThisStatus = false;
            if (node.assocs["icase:caseStatusAssoc"] && node.assocs["icase:caseStatusAssoc"].length > 0) {
                skipInThisStatus = (statuses.indexOf(node.assocs["icase:caseStatusAssoc"][0].nodeRef) >= 0);
            }
            try {
                if (!skipInThisStatus) {
                    if (checkPermission(node, key, permService)) {
                        attributes.set(node, key, attrs.get(key));
                        changeResult[key] = STATUS_SUCCESS;
                    } else {
                        changeResult[key] = STATUS_PERMISSION_DENIED;
                    }
                } else {
                    changeResult[key] = STATUS_SKIPPED;
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