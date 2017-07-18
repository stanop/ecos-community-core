(function() {

    var nodeRef = args.nodeRef, nodeRefs = args.nodeRefs,
        type = args.type, types = args.types,
        viewId = args.viewId || null,
        views = {};


    if (nodeRefs || nodeRef) {
        nodeRefs = nodeRefs ? nodeRefs.split(",") : [ nodeRef ];

        for (var n = 0; n < nodeRefs.length; n++) {
            var node = search.findNode(nodeRefs[n]);
            if (node) {
                if (nodeViews.hasNodeView(node, viewId)) 
                    views[nodeRefs[n]] = nodeViews.getNodeView(node, viewId);
            }
        }
    } else if (types || type) {
        types = types ? types.split(",") : [ type ];
        for (var t = 0; t < types.length; t++) {
            if (nodeViews.hasNodeView(types[t], viewId)) 
                views[types[t]] = nodeViews.getNodeView(types[t], viewId);
        }
    }

    model.views = views;

})()