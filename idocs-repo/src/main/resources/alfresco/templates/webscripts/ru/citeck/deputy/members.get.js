(function() {

var status = args.status || "all";
    if (status == "all") status = "own,deputy,assistants";
var statuses = status.split(/,/);

var roleName = url.templateArgs.roleName;

var members = [];
for(var i in statuses) {	
	var status = statuses[i];
	var roleMembers = [];
	if(status == "own") {
		roleMembers = deputies.getRoleMembers(roleName);
	} else if(status == "manage") {
		if(deputies.isRoleManagedByMembers(roleName)) {
			roleMembers = deputies.getRoleMembers(roleName);
		}
	} else if(status == "deputy") {
		roleMembers = deputies.getRoleDeputies(roleName);
    } else if (status == "assistants") {
        roleMembers = deputies.getRoleAssistants(roleName);
	}
	
	for(var i in roleMembers) {
		var member = roleMembers[i];
		var available = availability.getUserAvailability(member.userName);
		if(args.available == "true" && !available) continue;
		if(args.available == "false" && available) continue;
        var manage = deputies.isRoleDeputiedByUser(roleName, member.userName);
        var assistant = deputies.isRoleAssistedToUser(roleName, member.userName);
		members.push({
			"user": member,
            "manage": manage,
			"deputy": deputies.isRoleDeputiedToUser(roleName, member.userName),
            "available": available,
            "isAssistant": assistant,
            "canDelete": deputies.isCanDeleteDeputeOrAssistantFromRole(roleName)
		});
	}
}

model.members = members;

})();