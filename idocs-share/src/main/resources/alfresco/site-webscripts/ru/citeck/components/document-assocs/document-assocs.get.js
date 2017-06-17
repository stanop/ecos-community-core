<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function getAssocList(assocs, listName, defaultList) {
    var listElements = assocs.childrenMap[listName];
    var list = [];
    if (listElements) {
        for (var i = 0; i < listElements.size(); i++) {
            var assocElements = listElements.get(i).childrenMap["association"];
            if (!assocElements) continue;
            for (var j = 0; j < assocElements.size(); j++) {
                var assoc = assocElements.get(j);
                var directed = assoc.attributes.directed == "false" ? false : true;
                list.push({
                    'name': assoc.attributes.name,
                    'direction': directed ? (assoc.attributes.direction || "both") : "undirected",
                    'directed': directed
                });
            }
        }
    }
    return list.length > 0 ? list : defaultList;
}

(function() {

    var nodeRef = args.nodeRef;
    if(!nodeRef) {
        return;
    }

    var nodeDetails = AlfrescoUtil.getNodeDetails(nodeRef, model.site);
    var documentType = nodeDetails && nodeDetails.item && nodeDetails.item.node && nodeDetails.item.node.type;
    
    if(!documentType) return;

    // global scope
    var assocConfig = config.scoped["DocumentLibrary"].associations;
    
    if(!assocConfig) {
        return;
    }
    
    var visible = getAssocList(assocConfig, "visible"),
        addable = getAssocList(assocConfig, "addable", visible),
        removeable = getAssocList(assocConfig, "removeable", visible);

    // type scopes
    var typeConfigs = assocConfig.childrenMap["doctype"];
    if(!typeConfigs) { return; }

    for(var i = 0, ii = typeConfigs.size(); i < ii; i++) {
        var typeConfig = typeConfigs.get(i),
            typeConfigId = typeConfig.getAttribute("id");
        if(!dictionary.isSubType(documentType, typeConfigId)) continue;
        
        var typeVisible = getAssocList(typeConfig, "visible");
        visible = visible.concat(typeVisible);
        addable = addable.concat(getAssocList(typeConfig, "addable", typeVisible));
        removeable = removeable.concat(getAssocList(typeConfig, "removeable", typeVisible));
    }
   

    if (args.columns) { model.columns = args.columns; }

    model.nodeRef = nodeRef;
    model.assocs = {
        visible: visible,
        addable: addable,
        removeable: removeable
    };
    
})();