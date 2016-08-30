<import resource="classpath:alfresco/templates/webscripts/ru/citeck/orgmeta/orgmeta.lib.js">

(function() {
    var roleGroups = deputies.getCurrentUserRoles();
    roleGroups = roleGroups.concat(deputies.getRolesDeputiedToCurrentUser());

    var roles = [];
    for(var i in roleGroups) {
        var group = roleGroups[i];
        roles.push({
            "group": group,
            "manage": deputies.isRoleDeputiedByCurrentUser(group.fullName),
            "deputy": deputies.isRoleDeputiedToCurrentUser(group.fullName)
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
        "deputy": false
    });

    var deputiedUsers = deputies.getUsersWhoHaveThisUserdeputy(person.properties.userName);
    for (var i in deputiedUsers) {
        roles.push({
            "group": deputiedUsers[i],
            "manage": false,
            "deputy": false
        });
    }

    model.roles = roles;
})();

model.groupTypes = getAllSubTypeDictionaries();