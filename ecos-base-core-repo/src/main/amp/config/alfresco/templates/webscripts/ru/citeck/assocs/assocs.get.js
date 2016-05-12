(function()
{
    function checkContentType(node, contentTypes) {
        var result = false;
        for (var i = 0; i < contentTypes.length; i++) {
            var type = contentTypes[i];
            if (type) {
                if (node.isSubType(type)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
    var nodeRef = args['nodeRef'];
    var assocTypes = args['assocTypes'];
    var contentTypes = args['contentTypes'];
    contentTypes = contentTypes == undefined ? [] : contentTypes.split(',');
    var addAssocs = args['addAssocs']||"true";
    if (assocTypes == undefined) assocTypes = "";
    var assocs = [];
    if(nodeRef != null && nodeRef.length != 0) {
        for each(type in assocTypes.split(','))
            if (type != '' && nodeRef != null && nodeRef.length != 0){
                var node = search.findNode(nodeRef);
                if (node !== null) {
                    var sourceAssocs = node.sourceAssocs[type];
                    var sourceNodeRef = [];
                    if (sourceAssocs == undefined) {
                        sourceAssocs = "";
                    } else {
                        for each(source in sourceAssocs)
                            if(source != null && source.length != 0 && source.hasPermission("Read")) {
                                if (contentTypes.length > 0 && !checkContentType(source, contentTypes))
                                    continue;
                                sourceNodeRef.push({
                                    'nodeRef': source.nodeRef.toString(),
                                    'name': source.name,
                                    'isFolder': source.isSubType("cm:folder").toString(),
                                    'isContent': source.isSubType("cm:content").toString(),
									'source': source
                                });
                            }
                    }
                    var targetAssocs = node.assocs[type];
                    var targetNodeRef = [];
                    if (targetAssocs == undefined) {
                        targetAssocs = "";
                    } else {
                        for each(target in targetAssocs)
                            if(target != null && target.length != 0 && target.hasPermission("Read")) {
                                if (contentTypes.length > 0 && !checkContentType(target, contentTypes))
                                    continue;
                                targetNodeRef.push({
                                    'nodeRef': target.nodeRef.toString(),
                                    'name': target.name,
                                    'isFolder': target.isSubType("cm:folder").toString(),
                                    'isContent': target.isSubType("cm:content").toString(),
									'target': target
                                });
                            }
                    }
                    var childAssocs = node.childAssocs[type];
                    var childNodeRef = [];
                    if (childAssocs == undefined) {
                        childAssocs = "";
                    } else {
                        for each(child in childAssocs)
                            if(child != null && child.length != 0 && child.hasPermission("Read")) {
                                if (contentTypes.length > 0 && !checkContentType(child, contentTypes))
                                    continue;
                                childNodeRef.push({
                                    'nodeRef': child.nodeRef.toString(),
                                    'name': child.name,
                                    'isFolder': child.isSubType("cm:folder").toString(),
                                    'isContent': child.isSubType("cm:content").toString(),
									'child': child
                                });
                            }
                    }
                    assocs.push({
                        'type': type,
                        'sources': sourceNodeRef,
                        'targets': targetNodeRef,
                        'children': childNodeRef
                    });
                }
            }
    }
    model.data = {
        'assocs': assocs,
        'addAssocs': addAssocs
    }

    var cacheMaxAge = args['cacheMaxAge'];

    if (cacheMaxAge) {
        cache.maxAge = cacheMaxAge;
    }

})()
