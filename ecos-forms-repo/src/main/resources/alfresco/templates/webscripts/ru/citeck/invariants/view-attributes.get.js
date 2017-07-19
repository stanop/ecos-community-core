(function() {

    var nodeRef = args.nodeRef, type = args.type,
        viewId = args.viewId || null,
        view;


    if (nodeRef) {
        var node = search.findNode(nodeRef);
        if (node && nodeViews.hasNodeView(node, viewId)) 
            view = nodeViews.getNodeView(node, viewId);
    } else if (type) {
        if (nodeViews.hasNodeView(type, viewId)) 
            view = nodeViews.getNodeView(type, viewId);
    }

    model.view = view;

})()