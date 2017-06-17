(function() {
    var site = siteService.getSite(args.site);
    if(!site) {
        status.setCode(status.STATUS_NOT_FOUND, "Site " + args.site + " not found");
        return;
    }
    
    var siteTypes = site.node.assocs["tk:siteDocumentTypes"] || [];
    var typeIndex = {};
    for(var i in siteTypes) {
        typeIndex[siteTypes[i].nodeRef] = true;
    }
    
    // add types
    var added = [];
    if(json.has("add")) {
        var typesToAdd = json.get("add");
        for(var i = 0, ii = typesToAdd.length(); i < ii; i++) {
            var typeRef = typesToAdd.get(i);
            var type = search.findNode(typeRef);
            if(!type) throw "Can't find node " + typeRef;
            if(typeIndex[typeRef]) continue; // already added
            site.node.createAssociation(type, "tk:siteDocumentTypes");
            added.push(type);
        }
    }
    
    // remove types
    var removed = [];
    if(json.has("remove")) {
        var typesToRemove = json.get("remove");
        for(var i = 0, ii = typesToRemove.length(); i < ii; i++) {
            var typeRef = typesToRemove.get(i);
            var type = search.findNode(typeRef);
            if(!type) throw "Can't find node " + typeRef;
            if(!typeIndex[typeRef]) continue; // already removed
            site.node.removeAssociation(type, "tk:siteDocumentTypes");
            removed.push(type);
        }
    }
    
    model.added = added;
    model.removed = removed;
})()