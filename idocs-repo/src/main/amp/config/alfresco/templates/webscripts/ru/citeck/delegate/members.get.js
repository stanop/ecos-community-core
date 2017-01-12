(function() {

var status = args.status || "all";
if(status == "all") status = "own,delegate";
var statuses = status.split(/,/);

var roleName = url.templateArgs.roleName;

var members = [];
for(var i in statuses) {	
	var status = statuses[i];
	var roleMembers = [];
	if(status == "own") {
		roleMembers = delegates.getRoleMembers(roleName);
	} else if(status == "manage") {
		if(delegates.isRoleManagedByMembers(roleName)) {
			roleMembers = delegates.getRoleMembers(roleName);
		}
	} else if(status == "delegate") {
		roleMembers = delegates.getRoleDelegates(roleName);
	}
	
	for(var i in roleMembers) {
		var member = roleMembers[i];
		var available = availability.getUserAvailability(member.userName);
		if(args.available == "true" && !available) continue;
		if(args.available == "false" && available) continue;
		
		members.push({
			"user": member,
			"manage": delegates.isRoleDelegatedByUser(roleName, member.userName),
			"delegate": delegates.isRoleDelegatedToUser(roleName, member.userName),
			"available": available
		});
	}
}

model.members = members;

})();