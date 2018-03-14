(function () {
    var nodeRef = args.nodeRef;
    var user = args.user;
    var role = args.role;
    var isRoleMember = false;

    if (nodeRef && user && role) {
        isRoleMember = caseRoleService.isRoleMember(nodeRef, role, user);
    }

    model.isRoleMember = isRoleMember;

})();