var branches = orgstruct.getAllTypedGroups('branch', false);
for(var i in branches) {
  branches[i].deleteGroup();
}

var roles = orgstruct.getAllTypedGroups('role', false);
for(var i in roles) {
  roles[i].deleteGroup();
}
