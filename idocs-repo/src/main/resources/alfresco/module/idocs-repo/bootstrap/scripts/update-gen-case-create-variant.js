(function() {
    var repositoryHelper = services.get('repositoryHelper'),
        companyhome = search.findNode(repositoryHelper.getCompanyHome()),
        createVariantNode = (companyhome.childrenByXPath("st:sites/cm:cases/cm:journals/journal:cat-doc-type-general-case/journal:default") || [])[0];

    if (createVariantNode) {
        createVariantNode.properties["journal:type"] = "ecos:case";
        createVariantNode.save();
    }
})();