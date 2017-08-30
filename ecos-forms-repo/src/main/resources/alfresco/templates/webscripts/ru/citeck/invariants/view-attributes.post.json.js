(function() {

    var jsonData = jsonUtils.toObject(json),
        nodeRefs = jsonData.nodeRefs || [], types = jsonData.types || [],
        viewId = jsonData.viewId || null,
        views = {};

    if (nodeRefs.length > 0) {
        for (var n = 0; n < nodeRefs.length; n++) {
            var node = search.findNode(nodeRefs[n]);
            if (node) {
                views[nodeRefs[n]] = nodeViews.hasNodeView(node, viewId) ? nodeViews.getNodeView(node, viewId) : [];
            }
        }
    } else if (types.length > 0) {
        for (var t = 0; t < types.length; t++) {
            views[types[t]] = nodeViews.hasNodeView(types[t], viewId) ? nodeViews.getNodeView(types[t], viewId) : [];
        }
    }

    model.views = views;

})()