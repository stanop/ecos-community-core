(function() {
    
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

    function getTitle(attributeName, dictionaryService, messageService) {
        var attQName = citeckUtils.createQName(attributeName);
        var attribute = dictionaryService.getProperty(attQName);
        if (!attribute) {
            attribute = dictionaryService.getAssociation(attQName);
        }
        return attribute ? attribute.getTitle(messageService) : null;
    }


    var dictionaryService = services.get("dictionaryService"),
        messageService = services.get("messageService");

    var nodeRef = args['nodeRef'],
        assocTypes = args['assocTypes'],
        contentTypes = args['contentTypes'] == undefined ? [] : contentTypes.split(','),
        addAssocs = args['addAssocs']||"true";

    if (assocTypes == undefined) assocTypes = "";
    var assocs = [];

    if(nodeRef != null && nodeRef.length != 0) {
        for each(type in assocTypes.split(','))
            if (type != '' && nodeRef != null && nodeRef.length != 0){
                var node = search.findNode(nodeRef);
                if (node !== null) {
                    var sourceAssocs = node.sourceAssocs[type], sourceNodeRef = [];
                    if (sourceAssocs == undefined) {
                        sourceAssocs = "";
                    } else {
                        for each(source in sourceAssocs)
                            if(source != null && source.length != 0 && source.hasPermission("Read")) {
                                if (contentTypes.length > 0 && !checkContentType(source, contentTypes))
                                    continue;

                                var titleSource = source.name;
                                if (source.properties["cm:title"] != null && source.properties["cm:title"] != "") {
                                    titleSource = source.properties["cm:title"];
                                }

                                var sourceProperties = {};
                                for (var s in source.properties) {
                                    var shortSourcePropertyName = utils.shortQName(s);
                                    sourceProperties[shortSourcePropertyName] = {
                                        value: source.properties[s],
                                        label: getTitle(shortSourcePropertyName, dictionaryService, messageService)
                                    };
                                }

                                sourceNodeRef.push({
                                    'nodeRef': source.nodeRef.toString(),
                                    'name': source.name,
                                    'isFolder': source.isSubType("cm:folder").toString(),
                                    'isContent': source.isSubType("cm:content").toString(),
                                    'source': source,
                                    'title': titleSource
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

                                var titleTarget = target.name;
                                if (target.properties["cm:title"] != null && target.properties["cm:title"] != "") {
                                    titleTarget = target.properties["cm:title"];
                                }

                                var targetProperties = {};
                                for (var p in target.properties) {
                                    var shortTargetPropertyName = utils.shortQName(p);
                                    targetProperties[shortTargetPropertyName] = {
                                        value: target.properties[p],
                                        label: getTitle(shortTargetPropertyName, dictionaryService, messageService)
                                    };
                                }

                                targetNodeRef.push({
                                    'nodeRef': target.nodeRef.toString(),
                                    'name': target.name,
                                    'isFolder': target.isSubType("cm:folder").toString(),
                                    'isContent': target.isSubType("cm:content").toString(),
                                    'target': target,
                                    'title': titleTarget,
                                    'properties': targetProperties
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

                                var titleChild = child.name;
                                if (child.properties["cm:title"] != null && child.properties["cm:title"] != "") {
                                    titleChild = child.properties["cm:title"];
                                }

                                var childProperties = {};
                                for (var c in child.properties) {
                                    var shortChildPropertyName = utils.shortQName(c);
                                    childProperties[shortChildPropertyName] = {
                                        value: child.properties[c],
                                        label: getTitle(shortChildPropertyName, dictionaryService, messageService)
                                    };
                                }

                                childNodeRef.push({
                                    'nodeRef': child.nodeRef.toString(),
                                    'name': child.name,
                                    'isFolder': child.isSubType("cm:folder").toString(),
                                    'isContent': child.isSubType("cm:content").toString(),
                                    'child': child,
                                    'title': titleChild,
                                    'properties': childProperties
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

})();
