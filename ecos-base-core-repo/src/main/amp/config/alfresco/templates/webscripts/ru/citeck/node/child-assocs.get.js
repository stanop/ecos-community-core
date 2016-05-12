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
})();