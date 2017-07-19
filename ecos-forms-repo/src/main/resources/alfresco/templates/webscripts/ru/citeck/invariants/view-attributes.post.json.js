(function() {

    var jsonData = jsonUtils.toObject(json),
        nodeRefs = jsonData.nodeRefs || [], types = jsonData.types || [],
        viewId = jsonData.viewId || null,
        views = {};

    if (nodeRefs.length > 0) {
        for (var n = 0; n < nodeRefs.length; n++) {
            var node = search.findNode(nodeRefs[n]);
            if (node) {
                if (nodeViews.hasNodeView(node, viewId)) 
                    views[nodeRefs[n]] = nodeViews.getNodeView(node, viewId);
            }
        }
    } else if (types.length > 0) {
        for (var t = 0; t < types.length; t++) {
            if (nodeViews.hasNodeView(types[t], viewId)) 
                views[types[t]] = nodeViews.getNodeView(types[t], viewId);
        }
    }

    model.views = views;

})()