<import resource="classpath:alfresco/templates/webscripts/ru/citeck/orgmeta/orgmeta.lib.js">

(function() {
    var roleGroups = delegates.getCurrentUserRoles();
    roleGroups = roleGroups.concat(delegates.getRolesDelegatedToCurrentUser());

    var roles = [];
    for(var i in roleGroups) {
        var group = roleGroups[i];
        roles.push({
            "group": group,
            "manage": delegates.isRoleDelegatedByCurrentUser(group.fullName),
            "delegate": delegates.isRoleDelegatedToCurrentUser(group.fullName)
        });
    }
    var user = groups.getUser(person.properties.userName);
    if(!user) {
        status.setCode(status.STATUS_NOT_FOUND, "No such user: " + userName);
        return;
    }
    roles.push({
        "group": user,
        "manage": true,
        "delegate": false
    });

    var delegatedUsers = delegates.getUsersWhoHaveThisUserDelegate(person.properties.userName);
    for (var i in delegatedUsers) {
        roles.push({
            "group": delegatedUsers[i],
            "manage": false,
            "delegate": false
        });
    }

    model.roles = roles;
})();

model.groupTypes = getAllSubTypeDictionaries();