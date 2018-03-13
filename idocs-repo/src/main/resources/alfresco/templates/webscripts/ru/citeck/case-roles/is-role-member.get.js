(function () {
    var nodeRef = args.nodeRef;
    var user = args.user;
    var role = args.role;

    if (!nodeRef || !user || !role) {
        status.setStatus(status.STATUS_BAD_REQUEST, "Argument is missing");
        return;
    }

    var isRoleMember = caseRoleService.isRoleMember(nodeRef, role, user);

    model.isRoleMember = isRoleMember.toString();

})();