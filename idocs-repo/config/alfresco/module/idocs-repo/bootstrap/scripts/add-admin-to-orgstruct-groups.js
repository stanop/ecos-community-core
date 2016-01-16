var demoGroups = [
    {"name" : "GROUP_company_clerks"},
    {"name" : "GROUP_company_accountancy"},
    {"name" : "GROUP_company"},
    {"name" : "GROUP_company_chief_accountant"},
    {"name" : "GROUP_clerks"},
    {"name" : "GROUP_company_accountant"},
    {"name" : "GROUP_company_director"}
];

function userInGroup(group, userName) {
    var allUsers = group.allUsers;
    for(var i in allUsers) {
        if(allUsers[i].userName == userName)
            return true;
    }
    return false;
}

for (var i = 0; i < demoGroups.length; i++) {
    var group = groups.getGroupForFullAuthorityName(demoGroups[i].name);
    if(!group) continue;
    if(userInGroup(group, "admin")) continue;
    group.addAuthority("admin");
}
