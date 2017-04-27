(function() {
    var site = siteService.getSite(args.site);
    if(!site) {
        status.setCode(status.STATUS_NOT_FOUND, "Site " + args.site + " not found");
        return;
    }
    
    var root = search.findNode("workspace://SpacesStore/category-document-type-root");
    model.allTypes = root.childAssocs["cm:subcategories"] || [];
    model.siteTypes = site.node.assocs["tk:siteDocumentTypes"] || [];
})()