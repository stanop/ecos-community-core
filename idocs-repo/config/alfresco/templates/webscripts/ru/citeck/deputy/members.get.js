(function() {

var status = args.status || "all";
if(status == "all") status = "own,deputy";
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
	}
	
	for(var i in roleMembers) {
		var member = roleMembers[i];
		var available = availability.getUserAvailability(member.userName);
		if(args.available == "true" && !available) continue;
		if(args.available == "false" && available) continue;
		
		members.push({
			"user": member,
			"manage": deputies.isRoleDeputiedByUser(roleName, member.userName),
			"deputy": deputies.isRoleDeputiedToUser(roleName, member.userName),
			"available": available
		});
	}
}

model.members = members;

})();