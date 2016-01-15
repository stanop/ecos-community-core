var demoGroups = [
    {"name" : "GROUP_company_clerks"},
    {"name" : "GROUP_company_accountancy"},
    {"name" : "GROUP_company"},
    {"name" : "GROUP_company_chief_accountant"},
    {"name" : "GROUP_clerks"},
    {"name" : "GROUP_company_accountant"},
    {"name" : "GROUP_company_director"}
];

function isDemoGroup(groupName) {
    for (var i = 0; i < demoGroups.length; i++) {
        if (groupName.localeCompare(demoGroups[i].name)) {
            return true;
        }
    }
    return false;
}

var groupTypes = orgstruct.getGroupTypes();
for (var i = 0; i < groupTypes.length; i++) {
    var groupList = orgstruct.getAllTypedGroups(groupTypes[i], false);
    for (var j = 0; j < groupList.length; j++) {
        if (isDemoGroup(groupList[j].getFullName())) {
            groupList[j].addAuthority("admin");
        }
    }
}