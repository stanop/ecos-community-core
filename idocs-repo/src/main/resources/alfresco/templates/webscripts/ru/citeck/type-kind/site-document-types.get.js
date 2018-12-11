(function() {

    var site = siteService.getSite(args.site);
    if (!site) {
        status.setCode(status.STATUS_NOT_FOUND, "Site " + args.site + " not found");
        return;
    }

    var siteTypes = (site.node.assocs["tk:siteDocumentTypes"] || []).map(function(t) {
        return t.nodeRef + "";
    });

    var formatType = function(type) {
        var typeRef = type.nodeRef + "";
        return {
            nodeRef: typeRef,
            name: type.name,
            title: type.properties['cm:title'] || type.name,
            onSite: siteTypes.indexOf(typeRef) >= 0
        }
    };

    var root = search.findNode("workspace://SpacesStore/category-document-type-root");
    model.json = {
        types: (root.childAssocs["cm:subcategories"] || []).map(formatType)
    }
})();

