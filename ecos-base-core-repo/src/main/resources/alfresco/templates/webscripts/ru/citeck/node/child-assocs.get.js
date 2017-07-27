(function() {
    if(!args.nodeRef) {
        status.setStatus(status.STATUS_BAD_REQUEST, "Parameter 'nodeRef' is mandatory");
        return;
    }
    var node = search.findNode(args.nodeRef);
    if(!node) {
        status.setStatus(status.STATUS_NOT_FOUND, "Node " + args.nodeRef + " is not found");
        return;
    }
    model.nodes = (args.assocType != null ? node.childAssocs[args.assocType] : node.children) || [];

    if (args.docKind) {
        model.nodes = model.nodes.filter(function (node) {
            return node.properties["tk:kind"] && node.properties["tk:kind"].nodeRef == args.docKind;
        });
    }

    if (args.filterKind) {
        model.nodes = model.nodes.filter(function (node) {
            return !node.properties["tk:kind"] || args.filterKind.indexOf(node.properties["tk:kind"].nodeRef) == -1;
        });
    }
})();