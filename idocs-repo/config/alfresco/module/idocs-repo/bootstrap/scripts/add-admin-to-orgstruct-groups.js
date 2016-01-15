var groupTypes = orgstruct.getGroupTypes();
for (var i = 0; i < groupTypes.length; i++) {
    var groupList = orgstruct.getAllTypedGroups(groupTypes[i], false);
    for (var j = 0; j < groupList.length; j++) {
        groupList[j].addAuthority("admin");
    }
}