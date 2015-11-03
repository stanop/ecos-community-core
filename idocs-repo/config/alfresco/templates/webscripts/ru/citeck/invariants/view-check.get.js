(function() {
    
    var nodeRef = args.nodeRef,
        type = args.type,
        viewId = args.viewId,
        exists,
        defaultExists;
    
    if(type) {
        exists = nodeViews.hasNodeView(type, viewId);
        defaultExists = viewId ? nodeViews.hasNodeView(type, null) : exists;
    } else if(nodeRef) {
        var node = search.findNode(nodeRef);
        if(!node) {
            status.setCode(status.STATUS_NOT_FOUND, "Node " + nodeRef + " was not found");
            return;
        }
        exists = nodeViews.hasNodeView(node, viewId);
        defaultExists = viewId ? nodeViews.hasNodeView(node, null) : exists;
    } else {
        status.setCode(status.STATUS_BAD_REQUEST, "Either type or nodeRef should be specified");
        return;
    }
    
    model.exists = exists;
    model.defaultExists = defaultExists;
})()