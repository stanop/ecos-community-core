(function() {
    var authorityContainers = search.query({
        query: 'TYPE:"cm:authorityContainer"',
        language: 'fts-alfresco',
        page: {
            maxItems: 10000
        }
    });

    function getTypeScriptNode(type, name) {
        var nodes = search.query({
            query: 'TYPE:"' + type + '" AND =cm\\:name:"' + name + '"',
            language: 'fts-alfresco',
            page: {
                maxItems: 10000
            }
        });
        return nodes[0] || null;
    }

    if (authorityContainers && authorityContainers.length) {
        authorityContainers.forEach(function(authorityContainer) {
            var branchType = authorityContainer.properties['org:branchType'],
                branchTypeAssocs = authorityContainer.assocs['org:branchTypeAssoc'],
                roleType = authorityContainer.properties['org:roleType'],
                roleTypeAssocs = authorityContainer.assocs['org:roleTypeAssoc'];
            if (branchType && (!branchTypeAssocs || !branchTypeAssocs.length)) {
                var typeItem = getTypeScriptNode('org:simpleBranchType', branchType);
                if (typeItem) {
                    authorityContainer.createAssociation(typeItem, 'org:branchTypeAssoc');
                }
            } else if (roleType && (!roleTypeAssocs || !roleTypeAssocs.length)) {
                var typeItem = getTypeScriptNode('org:simpleRoleType', roleType);
                if (typeItem) {
                    authorityContainer.createAssociation(typeItem, 'org:roleTypeAssoc');
                }
            }
        });
    }
})();