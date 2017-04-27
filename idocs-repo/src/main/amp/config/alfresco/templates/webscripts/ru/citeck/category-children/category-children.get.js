(function() {
    if (args.nodeRef) {
        var category = classification.getCategory(args.nodeRef),
            children = category.children;
        var result = [];

        if (args.recurse && args.recurse == "true") {            

            for (var c in children) {
                result.push(createResultObject(children[c], 0));

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
            array.push(createResultObject(children[i], depth));

            if (children[i].hasChildren) {
                getChildren(array, children[i], depth)
            }
        }
    }

    function createResultObject(scriptNode, depth){

        var obj = {
                name: scriptNode.name,
                properties: {title: scriptNode.properties.title, 
                            description: scriptNode.properties.description},
                nodeRef: scriptNode.nodeRef.toString(),
                hasChildren: scriptNode.hasChildren                
            };

        if(arguments.length > 1){
            obj.depth = depth;
        }

        if (depth && depth > 0) {
            obj.parentNodeRef = scriptNode.getParent().nodeRef.toString();
        };
        return obj;     
    }
})()