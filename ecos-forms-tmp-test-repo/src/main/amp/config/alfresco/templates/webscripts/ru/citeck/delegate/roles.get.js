(function() {

var status = args.status || "all";
if(status == "all") status = "own,delegate";
var statuses = status.split(/,/);

var userName = url.templateArgs.userName;
var currentUserMode = userName == person.properties.userName;

var roles = [];
for(var i in statuses) {	
	var status = statuses[i];
	var roleGroups = [];
	if(status == "own") {
		roleGroups = currentUserMode ?
			delegates.getCurrentUserRoles() :
			delegates.getUserRoles(userName);
	} else if(status == "manage") {
		roleGroups = currentUserMode ? 
			delegates.getRolesDelegatedByCurrentUser() :
			delegates.getRolesDelegatedByUser(userName);
	} else if(status == "delegate") {
		roleGroups = currentUserMode ? 
			delegates.getRolesDelegatedToCurrentUser() :
			delegates.getRolesDelegatedToUser(userName);
	}
	
	for(var i in roleGroups) {
		var group = roleGroups[i];
		roles.push({
			"group": group,
			"manage": delegates.isRoleDelegatedByUser(group.fullName, userName),
			"delegate": delegates.isRoleDelegatedToUser(group.fullName, userName)
		});
	}
}

model.roles = roles;

})();