(function() {

var status = args.status || "all";
if(status == "all") status = "own,deputy";
var statuses = status.split(/,/);

var userName = url.templateArgs.userName;
var currentUserMode = userName == person.properties.userName;

var roles = [];
for(var i in statuses) {	
	var status = statuses[i];
	var roleGroups = [];
	if(status == "own") {
		roleGroups = currentUserMode ?
			deputies.getCurrentUserRoles() :
			deputies.getUserRoles(userName);
	} else if(status == "manage") {
		roleGroups = currentUserMode ? 
			deputies.getRolesDeputiedByCurrentUser() :
			deputies.getRolesDeputiedByUser(userName);
	} else if(status == "deputy") {
		roleGroups = currentUserMode ? 
			deputies.getRolesDeputiedToCurrentUser() :
			deputies.getRolesDeputiedToUser(userName);
	}
	
	for(var i in roleGroups) {
		var group = roleGroups[i];
		roles.push({
			"group": group,
			"manage": deputies.isRoleDeputiedByUser(group.fullName, userName),
			"deputy": deputies.isRoleDeputiedToUser(group.fullName, userName)
		});
	}
}

model.roles = roles;

})();