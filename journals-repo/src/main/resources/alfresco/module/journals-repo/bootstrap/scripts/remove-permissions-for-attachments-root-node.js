(function() {
    var attachRootNode = search.findNode("workspace://SpacesStore/attachments-root");
    if (!!attachRootNode) {
        attachRootNode.setInheritsPermissions(false);
        attachRootNode.removePermission("Contributor", "GROUP_EVERYONE");
    }
})();