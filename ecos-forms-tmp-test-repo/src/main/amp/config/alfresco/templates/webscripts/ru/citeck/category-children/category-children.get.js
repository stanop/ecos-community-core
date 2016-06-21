(function() {
    if (args.nodeRef) {
        var category = classification.getCategory(args.nodeRef),
            children = category.children;

        if (args.recurse && args.recurse == "true") {
            var result = [];

            for (var c in children) {
                result.push({
                   name: children[c].name,
                   nodeRef: children[c].nodeRef.toString(),
                   hasChildren: children[c].hasChildren,
                   depth: 0
                });

                if (children[c].hasChildren) {
                    getChildren(result, children[c], 0);
                }
            }

            model.data = result;
        } else {
            model.data = children;
        }       
    } else {
        status.setCode(status.STATUS_BAD_REQUEST, "Source node is not specified");
        return;
    }

    // RECURSE FUNCTIONS
    function getChildren(array, child, depth) {
        var children = child.children,
        childPosition = array.indexOf(child);

        depth = depth + 1;

        for (var i in children) {
            array.push({
                name: children[i].name,
                nodeRef: children[i].nodeRef.toString(),
                hasChildren: children[i].hasChildren,
                depth: depth,
                parentNodeRef: children[i].nodeRef.toString()
            });

            if (children[i].hasChildren) {
                getChildren(array, children[i], depth)
            }
        }
    }
})()