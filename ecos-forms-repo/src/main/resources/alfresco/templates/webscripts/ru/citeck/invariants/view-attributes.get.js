(function() {

    var nodeRef = args.nodeRef, type = args.type, viewId = args.viewId,
        nodeView, attributeNames = [];

    if (!viewId) viewId = null;

    if(nodeRef) {
        var node = search.findNode(nodeRef);
        if(!node) {
            status.setCode(status.STATUS_NOT_FOUND, "Node " + nodeRef + " was not found");
            return;
        }

        if (nodeViews.hasNodeView(node, viewId)) nodeView = nodeViews.getNodeView(node, viewId);
    } else if (type) {
        if (nodeViews.hasNodeView(type, viewId)) nodeView = nodeViews.getNodeView(type, viewId);
    }

    model.view = nodeView;

})()