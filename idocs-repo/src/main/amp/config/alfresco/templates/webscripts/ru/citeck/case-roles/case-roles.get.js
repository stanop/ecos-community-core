(function() {
    var nodeRef = args.nodeRef;
    if (!nodeRef) {
        status.setStatus(status.STATUS_BAD_REQUEST, "Argument nodeRef is missing");
        return;
    }

    model.roles = caseRoleService.getRoles(nodeRef);

})();